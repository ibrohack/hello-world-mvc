package tartanga.dami2.din.helloworldmvc.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;
import tartanga.dami2.din.helloworldmvc.exception.ConfigurationException;
import tartanga.dami2.din.helloworldmvc.exception.DaoException;
import tartanga.dami2.din.helloworldmvc.exception.InvalidCredentialsException;
import tartanga.dami2.din.helloworldmvc.model.User;

/**
 * Integration tests of {@link UserDaoDbImpl} against the MySQL database set in the
 * {@code config.properties} file of the project.
 *
 * <p>The tests are skipped when that file does not exist, when it selects the file
 * implementation or when the database cannot be reached, so the build also works on
 * computers without MySQL. They expect the demo users created by
 * {@code database/hello_world_mvc.sql}.
 *
 * @author Aritz Navarro
 * @author Brayan Romero
 * @author Ekaitz Rivero
 * @version 1.0.0
 */
class UserDaoDbImplTest {

    /** Database settings read from the configuration file of the project. */
    private static DaoConfig config;

    /** Data access object under test. */
    private static UserDao userDao;

    /**
     * Reads the configuration of the project and checks that its database can be
     * reached; otherwise all the tests of this class are skipped.
     */
    @BeforeAll
    static void connectToTheConfiguredDatabase() {
        Path configFile = Path.of(DaoConfig.DEFAULT_CONFIG_FILE);
        assumeTrue(Files.isRegularFile(configFile), "config.properties not found: database tests skipped");
        try {
            config = DaoConfig.load(configFile);
        } catch (ConfigurationException e) {
            throw new TestAbortedException("Invalid config.properties: database tests skipped", e);
        }
        assumeTrue(config.getDaoType() == DaoType.DATABASE,
                "config.properties does not select the database: database tests skipped");
        // Skip the tests when MySQL is not running or the credentials are not valid
        try (Connection connection = DriverManager.getConnection(
                config.getDbUrl(), config.getDbUser(), config.getDbPassword())) {
            assumeTrue(connection.isValid(5), "The database does not answer: database tests skipped");
        } catch (SQLException e) {
            throw new TestAbortedException("The database cannot be reached: database tests skipped", e);
        }
        userDao = new UserDaoDbImpl(config.getDbUrl(), config.getDbUser(), config.getDbPassword());
    }

    /**
     * Checks that the demo user is returned when the credentials match.
     *
     * @throws Exception if the lookup fails
     */
    @Test
    void getUserReturnsTheDemoUser() throws Exception {
        User user = userDao.getUser("demo", "Demo1234");

        assertEquals("demo", user.getLogin());
        assertEquals("Demo User", user.getFullName());
        assertEquals("demo@example.com", user.getEmail());
        assertEquals(LocalDate.of(2000, 1, 15), user.getBirthDate());
    }

    /**
     * Checks that the login is compared ignoring case.
     *
     * @throws Exception if the lookup fails
     */
    @Test
    void getUserIgnoresTheCaseOfTheLogin() throws Exception {
        assertEquals("demo", userDao.getUser("DeMo", "Demo1234").getLogin());
    }

    /**
     * Checks that a wrong password is rejected.
     */
    @Test
    void getUserThrowsWhenThePasswordIsWrong() {
        assertThrows(InvalidCredentialsException.class, () -> userDao.getUser("demo", "wrong"));
    }

    /**
     * Checks that an unknown login is rejected with the same exception as a wrong password.
     */
    @Test
    void getUserThrowsWhenTheLoginIsUnknown() {
        assertThrows(InvalidCredentialsException.class, () -> userDao.getUser("nobody", "Demo1234"));
    }

    /**
     * Checks that a connection refused by the database is reported as a data access error.
     */
    @Test
    void getUserThrowsDaoExceptionWhenTheDatabaseRejectsTheAccount() {
        UserDao rejectedDao = new UserDaoDbImpl(config.getDbUrl(), config.getDbUser(), "wrong-password");

        assertThrows(DaoException.class, () -> rejectedDao.getUser("demo", "Demo1234"));
    }
}
