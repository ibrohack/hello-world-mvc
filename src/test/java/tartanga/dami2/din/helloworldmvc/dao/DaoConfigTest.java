package tartanga.dami2.din.helloworldmvc.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tartanga.dami2.din.helloworldmvc.exception.ConfigurationException;

/**
 * Tests of {@link DaoConfig}.
 *
 * @author Aritz Navarro
 * @author Brayan Romero
 * @author Ekaitz Rivero
 * @version 1.0.0
 */
class DaoConfigTest {

    /** Temporary folder where each test writes its configuration file. */
    @TempDir
    Path tempDir;

    /**
     * Checks that the file implementation is read and that a relative users file path
     * is resolved against the folder of the configuration file.
     *
     * @throws Exception if the test cannot write or load the configuration
     */
    @Test
    void loadReadsTheFileImplementation() throws Exception {
        Path configFile = writeConfig("dao.implementation = file", "file.path = data/users.properties");

        DaoConfig config = DaoConfig.load(configFile);

        assertEquals(DaoType.FILE, config.getDaoType());
        assertEquals(tempDir.resolve("data").resolve("users.properties").toAbsolutePath().normalize(),
                config.getUsersFile());
    }

    /**
     * Checks that the database settings are read and that the password keeps its spaces.
     *
     * @throws Exception if the test cannot write or load the configuration
     */
    @Test
    void loadReadsTheDatabaseSettings() throws Exception {
        Path configFile = writeConfig("dao.implementation=DATABASE",
                "db.url=jdbc:mysql://localhost:3306/test", "db.user=tester", "db.password=pass word ");

        DaoConfig config = DaoConfig.load(configFile);

        assertEquals(DaoType.DATABASE, config.getDaoType());
        assertEquals("jdbc:mysql://localhost:3306/test", config.getDbUrl());
        assertEquals("tester", config.getDbUser());
        assertEquals("pass word ", config.getDbPassword());
    }

    /**
     * Checks that an empty database password is accepted, for accounts without one.
     *
     * @throws Exception if the test cannot write or load the configuration
     */
    @Test
    void loadAcceptsAnEmptyDatabasePassword() throws Exception {
        Path configFile = writeConfig("dao.implementation=DATABASE",
                "db.url=jdbc:mysql://localhost:3306/test", "db.user=root", "db.password=");

        assertEquals("", DaoConfig.load(configFile).getDbPassword());
    }

    /**
     * Checks that the {@code app.config} system property selects the configuration file.
     *
     * @throws Exception if the test cannot write or load the configuration
     */
    @Test
    void loadUsesTheFileGivenByTheSystemProperty() throws Exception {
        Path configFile = writeConfig("dao.implementation=FILE", "file.path=users.properties");
        System.setProperty(DaoConfig.CONFIG_PATH_PROPERTY, configFile.toString());
        try {
            assertEquals(DaoType.FILE, DaoConfig.load().getDaoType());
        } finally {
            // Restore the global state so that other tests are not affected
            System.clearProperty(DaoConfig.CONFIG_PATH_PROPERTY);
        }
    }

    /**
     * Checks that a missing configuration file is reported with a hint about the
     * example file.
     */
    @Test
    void loadFailsWhenTheFileDoesNotExist() {
        ConfigurationException exception = assertThrows(ConfigurationException.class,
                () -> DaoConfig.load(tempDir.resolve("missing.properties")));

        assertTrue(exception.getMessage().contains("config.properties.example"));
    }

    /**
     * Checks that an unknown data access implementation is reported with the list of
     * valid values.
     *
     * @throws IOException if the test cannot write the configuration
     */
    @Test
    void loadFailsWhenTheImplementationIsUnknown() throws IOException {
        Path configFile = writeConfig("dao.implementation=CLOUD");

        ConfigurationException exception = assertThrows(ConfigurationException.class,
                () -> DaoConfig.load(configFile));

        assertTrue(exception.getMessage().contains("Valid values"));
    }

    /**
     * Checks that a missing key required by the selected implementation is reported.
     *
     * @throws IOException if the test cannot write the configuration
     */
    @Test
    void loadFailsWhenARequiredKeyIsMissing() throws IOException {
        Path configFile = writeConfig("dao.implementation=DATABASE", "db.user=tester", "db.password=secret");

        ConfigurationException exception = assertThrows(ConfigurationException.class,
                () -> DaoConfig.load(configFile));

        assertTrue(exception.getMessage().contains("db.url"));
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
