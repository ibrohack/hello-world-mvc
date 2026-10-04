package tartanga.dami2.din.helloworldmvc.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tartanga.dami2.din.helloworldmvc.exception.DaoException;
import tartanga.dami2.din.helloworldmvc.exception.InvalidCredentialsException;
import tartanga.dami2.din.helloworldmvc.model.User;
import tartanga.dami2.din.helloworldmvc.util.PasswordHasher;

/**
 * Tests of {@link UserDaoFileImpl}.
 *
 * @author Aritz Navarro
 * @author Brayan Romero
 * @author Ekaitz Rivero
 * @version 1.0.0
 */
class UserDaoFileImplTest {

    /** Password of the user written by the tests. */
    private static final String PASSWORD = "Secret1234";

    /** Hash of {@link #PASSWORD}, created once because hashing is deliberately slow. */
    private static String passwordHash;

    /** Temporary folder where each test writes its users file. */
    @TempDir
    Path tempDir;

    /**
     * Creates the password hash shared by all the tests.
     */
    @BeforeAll
    static void hashPassword() {
        passwordHash = PasswordHasher.hash(PASSWORD);
    }

    /**
     * Checks that the data of the user is returned when the credentials match.
     *
     * @throws Exception if the test cannot write the file or the lookup fails
     */
    @Test
    void getUserReturnsTheUserWhenTheCredentialsMatch() throws Exception {
        UserDao userDao = new UserDaoFileImpl(writeUsers(demoUser("2000-01-15")));

        User user = userDao.getUser("demo", PASSWORD);

        assertEquals("demo", user.getLogin());
        assertEquals("Demo User", user.getFullName());
        assertEquals("demo@example.com", user.getEmail());
        assertEquals(LocalDate.of(2000, 1, 15), user.getBirthDate());
    }

    /**
     * Checks that the login is compared ignoring case and surrounding spaces.
     *
     * @throws Exception if the test cannot write the file or the lookup fails
     */
    @Test
    void getUserIgnoresTheCaseOfTheLogin() throws Exception {
        UserDao userDao = new UserDaoFileImpl(writeUsers(demoUser("2000-01-15")));

        assertEquals("demo", userDao.getUser(" DEMO ", PASSWORD).getLogin());
    }

    /**
     * Checks that a wrong password is rejected.
     *
     * @throws IOException if the test cannot write the file
     */
    @Test
    void getUserThrowsWhenThePasswordIsWrong() throws IOException {
        UserDao userDao = new UserDaoFileImpl(writeUsers(demoUser("2000-01-15")));

        assertThrows(InvalidCredentialsException.class, () -> userDao.getUser("demo", "wrong"));
    }

    /**
     * Checks that an unknown login is rejected with the same exception as a wrong password.
     *
     * @throws IOException if the test cannot write the file
     */
    @Test
    void getUserThrowsWhenTheLoginIsUnknown() throws IOException {
        UserDao userDao = new UserDaoFileImpl(writeUsers(demoUser("2000-01-15")));

        assertThrows(InvalidCredentialsException.class, () -> userDao.getUser("nobody", PASSWORD));
    }

    /**
     * Checks that a missing users file is reported as a data access error.
     */
    @Test
    void getUserThrowsDaoExceptionWhenTheFileIsMissing() {
        UserDao userDao = new UserDaoFileImpl(tempDir.resolve("missing.properties"));

        DaoException exception = assertThrows(DaoException.class, () -> userDao.getUser("demo", PASSWORD));

        assertTrue(exception.getMessage().contains("not found"));
    }

    /**
     * Checks that an invalid birth date is reported as a data access error.
     *
     * @throws IOException if the test cannot write the file
     */
    @Test
    void getUserThrowsDaoExceptionWhenTheBirthDateIsInvalid() throws IOException {
        UserDao userDao = new UserDaoFileImpl(writeUsers(demoUser("15/01/2000")));

        assertThrows(DaoException.class, () -> userDao.getUser("demo", PASSWORD));
    }

    /**
     * Checks that the sample users file of the project accepts the demo passwords
     * documented in the README.
     *
     * @throws Exception if the sample file cannot be read or a lookup fails
     */
    @Test
    void sampleUsersFileAcceptsTheDocumentedDemoPasswords() throws Exception {
        UserDao userDao = new UserDaoFileImpl(Path.of("data", "users.properties"));

        assertEquals("Demo User", userDao.getUser("demo", "Demo1234").getFullName());
        assertEquals("John Doe", userDao.getUser("jdoe", "JohnDoe1234").getFullName());
        assertEquals("Alice Smith", userDao.getUser("asmith", "AliceSmith1234").getFullName());
    }

    /**
     * Checks that the name of the data store includes the name of the file.
     *
     * @throws IOException if the test cannot write the file
     */
    @Test
    void getDataSourceNameIncludesTheFileName() throws IOException {
        UserDao userDao = new UserDaoFileImpl(writeUsers(demoUser("2000-01-15")));

        assertEquals("Users file (users.properties)", userDao.getDataSourceName());
    }

    /**
     * Returns the lines that describe the demo user in a users file.
     *
     * @param birthDate the birth date to write, as text
     * @return the lines of the user
     */
    private static List<String> demoUser(String birthDate) {
        return List.of(
                "demo.passwordHash=" + passwordHash,
                "demo.firstName=Demo",
                "demo.lastName=User",
                "demo.email=demo@example.com",
                "demo.birthDate=" + birthDate);
    }

    /**
     * Writes a users file with the given lines in the temporary folder.
     *
     * @param lines lines of the file
     * @return the path of the file
     * @throws IOException if the file cannot be written
     */
    private Path writeUsers(List<String> lines) throws IOException {
        Path usersFile = tempDir.resolve("users.properties");
        Files.write(usersFile, lines, StandardCharsets.UTF_8);
        return usersFile;
    }
}
