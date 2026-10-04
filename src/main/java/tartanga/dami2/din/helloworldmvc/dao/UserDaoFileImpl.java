package tartanga.dami2.din.helloworldmvc.dao;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import tartanga.dami2.din.helloworldmvc.exception.DaoException;
import tartanga.dami2.din.helloworldmvc.exception.InvalidCredentialsException;
import tartanga.dami2.din.helloworldmvc.model.User;
import tartanga.dami2.din.helloworldmvc.util.PasswordHasher;

/**
 * Data access implementation that reads the users from a properties file.
 *
 * <p>Each user is described by five keys that start with their login, in lowercase:
 * <pre>
 * demo.passwordHash=pbkdf2_sha256$600000$...
 * demo.firstName=Demo
 * demo.lastName=User
 * demo.email=demo@example.com
 * demo.birthDate=2000-01-15
 * </pre>
 * The birth date uses the ISO format ({@code yyyy-MM-dd}). The file is read again on
 * every request, so changes to it are applied without restarting the application.
 *
 * @author Aritz Navarro
 * @author Brayan Romero
 * @author Ekaitz Rivero
 * @version 1.0.0
 */
public class UserDaoFileImpl implements UserDao {

    /** Suffix of the key that holds the password hash of a user. */
    private static final String PASSWORD_HASH_SUFFIX = ".passwordHash";

    /** Suffix of the key that holds the first name of a user. */
    private static final String FIRST_NAME_SUFFIX = ".firstName";

    /** Suffix of the key that holds the last name of a user. */
    private static final String LAST_NAME_SUFFIX = ".lastName";

    /** Suffix of the key that holds the email address of a user. */
    private static final String EMAIL_SUFFIX = ".email";

    /** Suffix of the key that holds the date of birth of a user. */
    private static final String BIRTH_DATE_SUFFIX = ".birthDate";

    /** Message of the exception thrown when the credentials do not match any user. */
    private static final String INVALID_CREDENTIALS_MESSAGE = "Incorrect login or password";

    /** Logger of this class. */
    private static final Logger LOGGER = Logger.getLogger(UserDaoFileImpl.class.getName());

    /** Path of the users file. */
    private final Path usersFile;

    /**
     * Creates a data access object that reads the users from the given file.
     *
     * @param usersFile path of the users file
     * @throws NullPointerException if {@code usersFile} is {@code null}
     */
    public UserDaoFileImpl(Path usersFile) {
        this.usersFile = Objects.requireNonNull(usersFile, "usersFile");
    }

    /**
     * Returns the user whose login and password match the given credentials, looking
     * up the keys of the user in the properties file.
     *
     * @param login    the login typed by the user
     * @param password the password typed by the user, in plain text
     * @return the user identified by the credentials
     * @throws DaoException                if the file cannot be read or the data of the user is invalid
     * @throws InvalidCredentialsException if no user matches the login and password
     */
    @Override
    public User getUser(String login, String password) throws DaoException, InvalidCredentialsException {
        // Logins are stored in lowercase, so the comparison ignores case
        String normalizedLogin = login.trim().toLowerCase(Locale.ROOT);
        LOGGER.log(Level.FINE, "Looking up user {0} in {1}", new Object[] {normalizedLogin, usersFile});
        Properties users = readUsers();
        String passwordHash = users.getProperty(normalizedLogin + PASSWORD_HASH_SUFFIX);
        // An unknown login and a wrong password produce the same exception on purpose
        if (passwordHash == null || !matches(password, passwordHash.trim(), normalizedLogin)) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }
        return new User(normalizedLogin,
                getField(users, normalizedLogin, FIRST_NAME_SUFFIX),
                getField(users, normalizedLogin, LAST_NAME_SUFFIX),
                getField(users, normalizedLogin, EMAIL_SUFFIX),
                parseBirthDate(getField(users, normalizedLogin, BIRTH_DATE_SUFFIX), normalizedLogin));
    }

    /**
     * Returns the name of the data store used by this implementation: the users file.
     *
     * @return {@code "Users file"} followed by the name of the file
     */
    @Override
    public String getDataSourceName() {
        return "Users file (" + usersFile.getFileName() + ")";
    }

    /**
     * Reads all the keys and values of the users file.
     *
     * @return the keys and values of the file
     * @throws DaoException if the file does not exist or cannot be read
     */
    private Properties readUsers() throws DaoException {
        Properties users = new Properties();
        // Read as UTF-8 so that names with accents are kept
        try (Reader reader = Files.newBufferedReader(usersFile, StandardCharsets.UTF_8)) {
            users.load(reader);
        } catch (NoSuchFileException e) {
            throw new DaoException("Users file not found: " + usersFile, e);
        } catch (IOException | IllegalArgumentException e) {
            // IllegalArgumentException is thrown for a malformed \\uXXXX escape sequence
            throw new DaoException("Cannot read the users file " + usersFile, e);
        }
        return users;
    }

    /**
     * Checks whether a password matches the hash stored for a user.
     *
     * @param password     the password typed by the user
     * @param passwordHash the hash stored in the file
     * @param login        the login of the user, used in error messages
     * @return {@code true} if the password matches the hash
     * @throws DaoException if the stored hash is malformed
     */
    private boolean matches(String password, String passwordHash, String login) throws DaoException {
        try {
            return PasswordHasher.verify(password, passwordHash);
        } catch (IllegalArgumentException e) {
            throw new DaoException("The password hash of user \"" + login + "\" in " + usersFile
                    + " is malformed", e);
        }
    }

    /**
     * Returns the value of one of the keys of a user, without surrounding spaces.
     *
     * @param users  keys and values read from the file
     * @param login  the login of the user
     * @param suffix the suffix of the key, for example {@code ".email"}
     * @return the value of the key
     * @throws DaoException if the key is missing or its value is empty
     */
    private String getField(Properties users, String login, String suffix) throws DaoException {
        String value = users.getProperty(login + suffix);
        if (value == null || value.isBlank()) {
            throw new DaoException("The key \"" + login + suffix + "\" is missing or empty in " + usersFile);
        }
        return value.trim();
    }

    /**
     * Converts the birth date stored in the file into a date.
     *
     * @param text  the stored birth date, in the format {@code yyyy-MM-dd}
     * @param login the login of the user, used in error messages
     * @return the date of birth
     * @throws DaoException if the text is not a valid date
     */
    private LocalDate parseBirthDate(String text, String login) throws DaoException {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new DaoException("Invalid birth date \"" + text + "\" of user \"" + login + "\" in "
                    + usersFile + " (expected format: yyyy-MM-dd)", e);
        }
    }
}
