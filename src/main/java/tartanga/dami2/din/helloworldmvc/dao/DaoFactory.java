package tartanga.dami2.din.helloworldmvc.dao;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import tartanga.dami2.din.helloworldmvc.exception.ConfigurationException;

/**
 * Creates the data access object used by the application, choosing the implementation
 * selected in {@code config.properties}.
 *
 * <p>This is the only class that knows the concrete implementations: the rest of the
 * application works with the {@link UserDao} interface, so a new implementation can be
 * added without changing the user interface.
 *
 * @author Aritz Navarro
 * @author Brayan Romero
 * @author Ekaitz Rivero
 * @version 1.0.0
 */
public final class DaoFactory {

    /** Logger of this class. */
    private static final Logger LOGGER = Logger.getLogger(DaoFactory.class.getName());

    /**
     * Prevents the creation of instances: this class only has static members.
     */
    private DaoFactory() {
    }

    /**
     * Creates the data access object selected in the configuration file found at its
     * default location.
     *
     * @return the data access object
     * @throws ConfigurationException if the configuration file is missing, cannot be read or has invalid values
     * @see DaoConfig#load()
     */
    public static UserDao createUserDao() throws ConfigurationException {
        return createUserDao(DaoConfig.load());
    }

    /**
     * Creates the data access object selected in the given configuration.
     *
     * @param config the configuration of the data access layer
     * @return a {@link UserDaoDbImpl} or a {@link UserDaoFileImpl}, as selected in the configuration
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public static UserDao createUserDao(DaoConfig config) {
        Objects.requireNonNull(config, "config");
        // The type selected in the configuration decides which implementation is created
        UserDao userDao = switch (config.getDaoType()) {
            case DATABASE -> new UserDaoDbImpl(config.getDbUrl(), config.getDbUser(), config.getDbPassword());
            case FILE -> new UserDaoFileImpl(config.getUsersFile());
        };
        LOGGER.log(Level.INFO, "Data access implementation in use: {0}", userDao.getDataSourceName());
        return userDao;
    }
}
