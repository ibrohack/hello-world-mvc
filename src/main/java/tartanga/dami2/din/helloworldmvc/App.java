package tartanga.dami2.din.helloworldmvc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import tartanga.dami2.din.helloworldmvc.controller.LoginController;
import tartanga.dami2.din.helloworldmvc.dao.DaoFactory;
import tartanga.dami2.din.helloworldmvc.dao.UserDao;
import tartanga.dami2.din.helloworldmvc.exception.ConfigurationException;

/**
 * Entry point of the Hello World MVC application.
 *
 * <p>It configures logging, creates the data access object selected in
 * {@code config.properties} and opens the sign-in window.
 *
 * @author Aritz Navarro
 * @author Brayan Romero
 * @author Ekaitz Rivero
 * @version 1.0.0
 */
public class App extends Application {

    /** Location of the logging configuration among the application resources. */
    private static final String LOGGING_CONFIG = "/logging.properties";

    /** Folder where the log files are written, relative to the working directory. */
    private static final Path LOG_FOLDER = Path.of("logs");

    /** Logger of this class. */
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    /**
     * Creates the application. JavaFX calls this constructor when the application is
     * launched.
     */
    public App() {
        // Nothing to initialise here: the work starts in start(Stage)
    }

    /**
     * Launches the application after configuring logging.
     *
     * @param args command-line arguments, passed on to JavaFX
     */
    public static void main(String[] args) {
        configureLogging();
        launch(args);
    }

    /**
     * Starts the application: creates the data access object and shows the sign-in
     * window. If the configuration is not valid, an error dialog explains the problem
     * and the application exits.
     *
     * @param primaryStage the main window, provided by JavaFX
     */
    @Override
    public void start(Stage primaryStage) {
        LOGGER.info("Starting Hello World MVC");
        try {
            // The DAO is created first, so configuration problems are reported before any window is shown
            UserDao userDao = DaoFactory.createUserDao();
            FXMLLoader loader = new FXMLLoader(LoginController.class.getResource(LoginController.VIEW));
            Parent root = loader.load();
            LoginController controller = loader.getController();
            controller.setStage(primaryStage);
            controller.setUserDao(userDao);
            controller.initStage(root);
        } catch (ConfigurationException e) {
            LOGGER.log(Level.SEVERE, "The configuration is not valid", e);
            showStartupError("The application cannot start because of a configuration problem.", e.getMessage());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "The sign-in window cannot be loaded", e);
            showStartupError("The sign-in window cannot be loaded.", e.getMessage());
        }
    }

    /**
     * Shows an error dialog when the application cannot start, and exits once the user
     * closes it.
     *
     * @param header  short description of the problem
     * @param details detailed explanation of the problem and how to fix it
     */
    private void showStartupError(String header, String details) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Hello World MVC");
        alert.setHeaderText(header);
        alert.setContentText(details);
        // Let the dialog grow so that long messages are not cut
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
        // No window has been shown, so JavaFX would not exit on its own
        Platform.exit();
    }

    /**
     * Configures {@code java.util.logging} with the {@code logging.properties} resource:
     * messages go to the console and to rotating files in the {@code logs} folder. If the
     * configuration cannot be loaded, the default configuration (console only) is kept.
     */
    private static void configureLogging() {
        try (InputStream config = App.class.getResourceAsStream(LOGGING_CONFIG)) {
            if (config == null) {
                LOGGER.log(Level.WARNING, "Logging configuration not found: {0}", LOGGING_CONFIG);
                return;
            }
            // The file handler does not create missing folders, so the log folder is created first
            Files.createDirectories(LOG_FOLDER);
            LogManager.getLogManager().readConfiguration(config);
        } catch (IOException e) {
            // The default configuration is still active, so the problem reaches the console
            LOGGER.log(Level.WARNING, "Logging configuration cannot be loaded", e);
        }
    }
}
