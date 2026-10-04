package tartanga.dami2.din.helloworldmvc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import tartanga.dami2.din.helloworldmvc.exception.DaoException;
import tartanga.dami2.din.helloworldmvc.exception.InvalidCredentialsException;
import tartanga.dami2.din.helloworldmvc.model.User;
import tartanga.dami2.din.helloworldmvc.util.PasswordHasher;

/**
 * Data access implementation that reads the users from a MySQL 8.0 database through
 * JDBC.
 *
 * <p>A connection is opened for each request and closed right after it with
 * try-with-resources: the application only runs one query per sign-in, so a connection
 * pool is not needed. The query uses a {@link PreparedStatement}, so the values typed by
 * the user can never change the SQL statement (SQL injection).
 *
 * <p>The class only uses the standard JDBC API: the MySQL driver is found on the class
 * path from the connection URL, so it is never referenced directly.
 *
 * @author Aritz Navarro
 * @author Brayan Romero
 * @author Ekaitz Rivero
 * @version 1.0.0
 */
public class UserDaoDbImpl implements UserDao {

    /** SQL query that returns the data of the user with a given login. */
    private static final String SELECT_USER_BY_LOGIN =
            "SELECT login, password_hash, first_name, last_name, email, birth_date "
            + "FROM app_user WHERE login = ?";

    /** Message of the exception thrown when the credentials do not match any user. */
    private static final String INVALID_CREDENTIALS_MESSAGE = "Incorrect login or password";

    /** Logger of this class. */
    private static final Logger LOGGER = Logger.getLogger(UserDaoDbImpl.class.getName());

    /** JDBC URL of the database. */
    private final String dbUrl;

    /** User name of the database account. */
    private final String dbUser;

    /** Password of the database account. */
    private final String dbPassword;

    /**
     * Creates a data access object that reads the users from the given database.
     * No connection is opened until the first request.
     *
     * @param dbUrl      JDBC URL of the database
     * @param dbUser     user name of the database account
     * @param dbPassword password of the database account
     * @throws NullPointerException if any argument is {@code null}
     */
    public UserDaoDbImpl(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = Objects.requireNonNull(dbUrl, "dbUrl");
        this.dbUser = Objects.requireNonNull(dbUser, "dbUser");
        this.dbPassword = Objects.requireNonNull(dbPassword, "dbPassword");
    }

    /**
     * Returns the user whose login and password match the given credentials, reading
     * the user from the {@code app_user} table.
     *
     * @param login    the login typed by the user
     * @param password the password typed by the user, in plain text
     * @return the user identified by the credentials
     * @throws DaoException                if the database cannot be accessed or the stored data is invalid
     * @throws InvalidCredentialsException if no user matches the login and password
     */
    @Override
    public User getUser(String login, String password) throws DaoException, InvalidCredentialsException {
        // Logins are stored in lowercase, so the comparison ignores case
        String normalizedLogin = login.trim().toLowerCase(Locale.ROOT);
        LOGGER.log(Level.FINE, "Looking up user {0} in the database", normalizedLogin);
        String passwordHash;
        User user;
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
                PreparedStatement statement = connection.prepareStatement(SELECT_USER_BY_LOGIN)) {
            // The login is sent as a parameter, never concatenated into the SQL text
            statement.setString(1, normalizedLogin);
            try (ResultSet resultSet = statement.executeQuery()) {
                // An unknown login and a wrong password produce the same exception on purpose
                if (!resultSet.next()) {
                    throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
                }
                passwordHash = resultSet.getString("password_hash");
                user = toUser(resultSet);
            }
        } catch (SQLException e) {
            throw new DaoException("Cannot read the user data from the database: " + e.getMessage(), e);
        }
        // The slow hash check runs after closing the connection, so it is not kept open meanwhile
        if (!matches(password, passwordHash, normalizedLogin)) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }
        return user;
    }

    /**
     * Returns the name of the data store used by this implementation: the MySQL database.
     *
     * @return {@code "MySQL database"}
     */
    @Override
    public String getDataSourceName() {
        return "MySQL database";
    }

    /**
     * Creates a user from the current row of a query result.
     *
     * @param resultSet the query result, positioned on the row of the user
     * @return the user described by the row
     * @throws SQLException if a column cannot be read
     */
    private User toUser(ResultSet resultSet) throws SQLException {
        return new User(resultSet.getString("login"),
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getString("email"),
                // JDBC 4.2 converts the DATE column directly into a LocalDate
                resultSet.getObject("birth_date", LocalDate.class));
    }

    /**
     * Checks whether a password matches the hash stored for a user.
     *
     * @param password     the password typed by the user
     * @param passwordHash the hash stored in the database
     * @param login        the login of the user, used in error messages
     * @return {@code true} if the password matches the hash
     * @throws DaoException if the stored hash is malformed
     */
    private boolean matches(String password, String passwordHash, String login) throws DaoException {
        try {
            return PasswordHasher.verify(password, passwordHash);
        } catch (IllegalArgumentException e) {
            throw new DaoException("The password hash stored for user \"" + login + "\" is malformed", e);
        }
    }
}
