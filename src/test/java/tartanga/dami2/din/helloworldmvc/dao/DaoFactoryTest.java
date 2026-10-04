package tartanga.dami2.din.helloworldmvc.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests of {@link DaoFactory}.
 *
 * @author Aritz Navarro
 * @author Brayan Romero
 * @author Ekaitz Rivero
 * @version 1.0.0
 */
class DaoFactoryTest {

    /** Temporary folder where each test writes its configuration file. */
    @TempDir
    Path tempDir;

    /**
     * Checks that the file implementation is created when the configuration selects it.
     *
     * @throws Exception if the test cannot write or load the configuration
     */
    @Test
    void createUserDaoReturnsTheFileImplementation() throws Exception {
        DaoConfig config = DaoConfig.load(writeConfig("dao.implementation=FILE", "file.path=users.properties"));

        UserDao userDao = DaoFactory.createUserDao(config);

        assertInstanceOf(UserDaoFileImpl.class, userDao);
        assertEquals("Users file (users.properties)", userDao.getDataSourceName());
    }

    /**
     * Checks that the database implementation is created when the configuration selects
     * it. No connection is opened, so the test does not need a database.
     *
     * @throws Exception if the test cannot write or load the configuration
     */
    @Test
    void createUserDaoReturnsTheDatabaseImplementation() throws Exception {
        DaoConfig config = DaoConfig.load(writeConfig("dao.implementation=DATABASE",
                "db.url=jdbc:mysql://localhost:3306/test", "db.user=tester", "db.password=secret"));

        UserDao userDao = DaoFactory.createUserDao(config);

        assertInstanceOf(UserDaoDbImpl.class, userDao);
        assertEquals("MySQL database", userDao.getDataSourceName());
    }

    /**
     * Writes a configuration file with the given lines in the temporary folder.
     *
     * @param lines lines of the file
     * @return the path of the file
     * @throws IOException if the file cannot be written
     */
    private Path writeConfig(String... lines) throws IOException {
        Path configFile = tempDir.resolve("config.properties");
        Files.write(configFile, List.of(lines), StandardCharsets.UTF_8);
        return configFile;
    }
}
