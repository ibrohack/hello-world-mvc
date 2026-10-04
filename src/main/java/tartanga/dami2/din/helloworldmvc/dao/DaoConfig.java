package tartanga.dami2.din.helloworldmvc.dao;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import tartanga.dami2.din.helloworldmvc.exception.ConfigurationException;

/**
 * Loads and validates the settings of the data access layer from the
 * {@code config.properties} file.
 *
 * <p>By default the file is read from the current working directory; the
 * {@value #CONFIG_PATH_PROPERTY} system property can point to another file. Each
 * developer creates the file by copying the tracked {@code config.properties.example}
 * and setting their own values. The real file is ignored by Git because it contains
 * database credentials.
 *
 * <p>Keys read from the file:
 * <ul>
 *   <li>{@code dao.implementation}: {@code DATABASE} or {@code FILE}; always required.</li>
 *   <li>{@code db.url}, {@code db.user} and {@code db.password}: connection settings,
 *       required by the {@code DATABASE} implementation. The password may be empty.</li>
 *   <li>{@code file.path}: path of the users file, required by the {@code FILE}
 *       implementation. A relative path is resolved against the folder of the
 *       configuration file.</li>
 * </ul>
 *
 * @author Aritz Navarro
 * @author Brayan Romero
 * @author Ekaitz Rivero
 * @version 1.0.0
 */
public final class DaoConfig {

    /** Name of the system property that sets the location of the configuration file. */
    public static final String CONFIG_PATH_PROPERTY = "app.config";

    /** Name of the configuration file used when the system property is not set. */
    public static final String DEFAULT_CONFIG_FILE = "config.properties";

    /** Key that selects the data access implementation. */
    private static final String DAO_IMPLEMENTATION_KEY = "dao.implementation";

    /** Key of the JDBC URL of the database. */
    private static final String DB_URL_KEY = "db.url";

    /** Key of the user name of the database account. */
    private static final String DB_USER_KEY = "db.user";

    /** Key of the password of the database account. */
    private static final String DB_PASSWORD_KEY = "db.password";

    /** Key of the path of the users file. */
    private static final String FILE_PATH_KEY = "file.path";

    /** Logger of this class. */
    private static final Logger LOGGER = Logger.getLogger(DaoConfig.class.getName());

    /** Data access implementation selected in the configuration file. */
    private final DaoType daoType;

    /** JDBC URL of the database, or {@code null} if the file implementation is selected. */
    private final String dbUrl;

    /** User name of the database account, or {@code null} if the file implementation is selected. */
    private final String dbUser;

    /** Password of the database account, or {@code null} if the file implementation is selected. */
    private final String dbPassword;

    /** Absolute path of the users file, or {@code null} if the database implementation is selected. */
    private final Path usersFile;

    /**
     * Creates a configuration with values that have already been validated. Instances
     * are created by the {@code load} methods.
     *
     * @param daoType    data access implementation selected in the file
     * @param dbUrl      JDBC URL of the database, or {@code null}
     * @param dbUser     user name of the database account, or {@code null}
     * @param dbPassword password of the database account, or {@code null}
     * @param usersFile  absolute path of the users file, or {@code null}
     */
    private DaoConfig(DaoType daoType, String dbUrl, String dbUser, String dbPassword, Path usersFile) {
        this.daoType = daoType;
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.usersFile = usersFile;
    }

    /**
     * Loads the configuration from its default location: the file named by the
     * {@value #CONFIG_PATH_PROPERTY} system property or, if it is not set,
     * {@value #DEFAULT_CONFIG_FILE} in the current working directory.
     *
     * @return the validated configuration
     * @throws ConfigurationException if the file is missing, cannot be read or has invalid values
     */
    public static DaoConfig load() throws ConfigurationException {
        String customLocation = System.getProperty(CONFIG_PATH_PROPERTY);
        String location = customLocation == null || customLocation.isBlank()
                ? DEFAULT_CONFIG_FILE
                : customLocation;
        return load(toPath(location, "configuration file"));
    }

    /**
     * Loads the configuration from the given file.
     *
     * @param path path of the configuration file
     * @return the validated configuration
     * @throws ConfigurationException if the file is missing, cannot be read or has invalid values
     */
    public static DaoConfig load(Path path) throws ConfigurationException {
        Path configFile = path.toAbsolutePath().normalize();
        Properties properties = readProperties(configFile);
        DaoType daoType = DaoType.parse(getValue(properties, DAO_IMPLEMENTATION_KEY, configFile));
        // Only the keys used by the selected implementation are required
        DaoConfig config = switch (daoType) {
            case DATABASE -> new DaoConfig(daoType,
                    getValue(properties, DB_URL_KEY, configFile),
                    getValue(properties, DB_USER_KEY, configFile),
                    getPassword(properties, configFile),
                    null);
            case FILE -> new DaoConfig(daoType, null, null, null,
                    resolveUsersFile(getValue(properties, FILE_PATH_KEY, configFile), configFile));
        };
        LOGGER.log(Level.INFO, "Configuration loaded from {0} (data access implementation: {1})",
                new Object[] {configFile, daoType});
        return config;
    }

    /**
     * Reads the keys and values of the configuration file.
     *
     * @param configFile absolute path of the configuration file
     * @return the keys and values read from the file
     * @throws ConfigurationException if the file does not exist or cannot be read
     */
    private static Properties readProperties(Path configFile) throws ConfigurationException {
        if (!Files.isRegularFile(configFile)) {
            throw new ConfigurationException("Configuration file not found: " + configFile
                    + ". Copy config.properties.example to config.properties and set your own values.");
        }
        Properties properties = new Properties();
        // Read as UTF-8 so that non-ASCII characters, for example in passwords, are kept
        try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException | IllegalArgumentException e) {
            // IllegalArgumentException is thrown for a malformed \\uXXXX escape sequence
            throw new ConfigurationException("Cannot read the configuration file " + configFile
                    + ". Check that it is a valid properties file saved in UTF-8.", e);
        }
        return properties;
    }

    /**
     * Returns the value of a required key, without surrounding spaces.
     *
     * @param properties keys and values read from the configuration file
     * @param key        the key to read
     * @param configFile path of the configuration file, used in error messages
     * @return the value of the key
     * @throws ConfigurationException if the key is missing or its value is empty
     */
    private static String getValue(Properties properties, String key, Path configFile)
            throws ConfigurationException {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new ConfigurationException("The key \"" + key + "\" is missing or empty in " + configFile);
        }
        return value.trim();
    }

    /**
     * Returns the database password. The key is required, but its value may be empty
     * for accounts without a password. It is not trimmed because spaces may be part of
     * the password.
     *
     * @param properties keys and values read from the configuration file
     * @param configFile path of the configuration file, used in error messages
     * @return the database password
     * @throws ConfigurationException if the key is missing
     */
    private static String getPassword(Properties properties, Path configFile) throws ConfigurationException {
        String password = properties.getProperty(DB_PASSWORD_KEY);
        if (password == null) {
            throw new ConfigurationException("The key \"" + DB_PASSWORD_KEY + "\" is missing in " + configFile);
        }
        return password;
    }

    /**
     * Converts the users file setting into an absolute path.
     *
     * @param filePath   value of the {@code file.path} key
     * @param configFile absolute path of the configuration file
     * @return the absolute path of the users file
     * @throws ConfigurationException if the value is not a valid path
     */
    private static Path resolveUsersFile(String filePath, Path configFile) throws ConfigurationException {
        Path path = toPath(filePath, "users file");
        // A relative path is resolved against the folder of the configuration file,
        // so the application works whatever the working directory is
        return path.isAbsolute() ? path.normalize() : configFile.getParent().resolve(path).normalize();
    }

    /**
     * Converts a text into a path.
     *
     * @param location    the text to convert
     * @param description what the path points to, used in error messages
     * @return the path
     * @throws ConfigurationException if the text is not a valid path
     */
    private static Path toPath(String location, String description) throws ConfigurationException {
        try {
            return Path.of(location);
        } catch (InvalidPathException e) {
            throw new ConfigurationException("Invalid path of the " + description + ": " + location, e);
        }
    }

    /**
     * Returns the data access implementation selected in the configuration file.
     *
     * @return the selected implementation
     */
    public DaoType getDaoType() {
        return daoType;
    }

    /**
     * Returns the JDBC URL of the database.
     *
     * @return the JDBC URL, or {@code null} if the file implementation is selected
     */
    public String getDbUrl() {
        return dbUrl;
    }

    /**
     * Returns the user name of the database account.
     *
     * @return the user name, or {@code null} if the file implementation is selected
     */
    public String getDbUser() {
        return dbUser;
    }

    /**
     * Returns the password of the database account.
     *
     * @return the password, or {@code null} if the file implementation is selected
     */
    public String getDbPassword() {
        return dbPassword;
    }

    /**
     * Returns the absolute path of the users file.
     *
     * @return the path of the users file, or {@code null} if the database implementation is selected
     */
    public Path getUsersFile() {
        return usersFile;
    }
}
