package tartanga.dami2.din.helloworldmvc.controller;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import tartanga.dami2.din.helloworldmvc.dao.UserDao;
import tartanga.dami2.din.helloworldmvc.exception.DaoException;
import tartanga.dami2.din.helloworldmvc.exception.InvalidCredentialsException;
import tartanga.dami2.din.helloworldmvc.model.User;
import tartanga.dami2.din.helloworldmvc.util.InputValidator;

/**
 * Controller of the sign-in window, defined in {@code LoginView.fxml}.
 *
 * <p>It checks the format of the login and password typed by the user, asks the data
 * access layer for the matching user in a background task, so that the window does not
 * freeze, and opens the user data window when the credentials are right.
 *
 * @author Aritz Navarro
 * @author Brayan Romero
 * @author Ekaitz Rivero
 * @version 1.0.0
 */
public class LoginController {

    /** Location of the FXML view controlled by this class, among the application resources. */
    public static final String VIEW = "/tartanga/dami2/din/helloworldmvc/view/LoginView.fxml";

    /** Title of the sign-in window. */
    private static final String TITLE = "Hello World MVC - Sign in";

    /** Logger of this class. */
    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    /** Text field where the user types the login. */
    @FXML
    private TextField loginField;

    /** Field where the user types the password; it hides the characters typed. */
    @FXML
    private PasswordField passwordField;

    /** Button that starts the sign-in; it is the default button of the window (Enter). */
    @FXML
    private Button signInButton;

    /** Label that shows validation and sign-in error messages. */
    @FXML
    private Label errorLabel;

    /** Indicator shown while a sign-in is in progress. */
    @FXML
    private ProgressIndicator progressIndicator;

    /** Whether a sign-in is in progress; while it is, the form is disabled. */
    private final BooleanProperty busy = new SimpleBooleanProperty(false);

    /** Window managed by this controller. */
    private Stage stage;

    /** Data access object used to look up the users. */
    private UserDao userDao;

    /**
     * Creates the controller. {@link javafx.fxml.FXMLLoader} calls this constructor when
     * it loads the view.
     */
    public LoginController() {
        // Nothing to initialise here: FXMLLoader injects the @FXML fields afterwards
    }

    /**
     * Sets the window managed by this controller.
     *
     * @param stage the window where the view is shown
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Sets the data access object used to look up the users.
     *
     * @param userDao the data access object
     */
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Shows the sign-in window with the given view.
     *
     * @param root root node of the loaded view
     */
    public void initStage(Parent root) {
        stage.setScene(new Scene(root));
        stage.setTitle(TITLE);
        stage.setResizable(false);
        stage.show();
        // The user can start typing straight away
        loginField.requestFocus();
    }

    /**
     * Prepares the controls once the view has been loaded. {@link javafx.fxml.FXMLLoader}
     * calls this method after injecting the {@code @FXML} fields.
     */
    @FXML
    private void initialize() {
        // Longer values would be rejected by the validation anyway, so they cannot be typed
        limitLength(loginField, InputValidator.LOGIN_MAX_LENGTH);
        limitLength(passwordField, InputValidator.PASSWORD_MAX_LENGTH);
        // The button is only enabled when both fields have text and no sign-in is in progress
        signInButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> loginField.getText().isBlank() || passwordField.getText().isEmpty() || busy.get(),
                loginField.textProperty(), passwordField.textProperty(), busy));
        loginField.disableProperty().bind(busy);
        passwordField.disableProperty().bind(busy);
        progressIndicator.visibleProperty().bind(busy);
        // Typing again hides the previous error message
        loginField.textProperty().addListener((observable, oldText, newText) -> clearError());
        passwordField.textProperty().addListener((observable, oldText, newText) -> clearError());
    }

    /**
     * Handles the sign-in button: checks the format of the values typed and, if it is
     * valid, starts the sign-in.
     */
    @FXML
    private void handleSignInAction() {
        String login = loginField.getText().trim();
        String password = passwordField.getText();
        // Check the format before contacting the data store
        if (!InputValidator.isValidLogin(login)) {
            LOGGER.fine("Sign-in rejected: the login has an invalid format");
            showError("The login must have " + InputValidator.LOGIN_MIN_LENGTH + " to "
                    + InputValidator.LOGIN_MAX_LENGTH
                    + " characters: letters, digits, dots, underscores or hyphens.", loginField);
            return;
        }
        if (!InputValidator.isValidPassword(password)) {
            LOGGER.fine("Sign-in rejected: the password has an invalid length");
            showError("The password must have 1 to " + InputValidator.PASSWORD_MAX_LENGTH + " characters.",
                    passwordField);
            return;
        }
        signIn(login, password);
    }

    /**
     * Handles the exit button: closes the application.
     */
    @FXML
    private void handleExitAction() {
        LOGGER.info("Application closed from the sign-in window");
        Platform.exit();
    }

    /**
     * Looks up the user in a background thread, so that the window keeps responding
     * while the data store is accessed and the password is checked.
     *
     * @param login    the login, already validated
     * @param password the password, already validated
     */
    private void signIn(String login, String password) {
        Task<User> signInTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                // Runs in the background thread, so it must not touch the controls
                return userDao.getUser(login, password);
            }
        };
        // These handlers run on the JavaFX Application Thread, so they can update the controls
        signInTask.setOnSucceeded(event -> handleSignInSuccess(signInTask.getValue()));
        signInTask.setOnFailed(event -> handleSignInFailure(login, signInTask.getException()));
        busy.set(true);
        Thread signInThread = new Thread(signInTask, "sign-in");
        // A daemon thread does not keep the application running if the window is closed
        signInThread.setDaemon(true);
        signInThread.start();
    }

    /**
     * Handles a successful sign-in: opens the user data window and closes the sign-in
     * window.
     *
     * @param user the signed-in user
     */
    private void handleSignInSuccess(User user) {
        busy.set(false);
        LOGGER.log(Level.INFO, "User {0} signed in", user.getLogin());
        try {
            FXMLLoader loader = new FXMLLoader(UserDataController.class.getResource(UserDataController.VIEW));
            Parent root = loader.load();
            UserDataController controller = loader.getController();
            controller.setStage(new Stage());
            controller.setUserDao(userDao);
            controller.setUser(user);
            controller.initStage(root);
            // The user data window is already open, so closing this one does not end the application
            stage.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "The user data window cannot be opened", e);
            showAlert(AlertType.ERROR, "The user data window cannot be opened.", e.getMessage());
        }
    }

    /**
     * Handles a failed sign-in, telling the user what went wrong.
     *
     * @param login     the login used in the attempt
     * @param exception the exception thrown by the data access layer
     */
    private void handleSignInFailure(String login, Throwable exception) {
        busy.set(false);
        if (exception instanceof InvalidCredentialsException) {
            LOGGER.log(Level.WARNING, "Failed sign-in attempt for login {0}", login);
            // The password is cleared before showing the message, because typing hides it
            passwordField.clear();
            showError("Incorrect login or password.", passwordField);
        } else if (exception instanceof DaoException) {
            LOGGER.log(Level.SEVERE, "The user data cannot be accessed", exception);
            showAlert(AlertType.ERROR, "The user data cannot be accessed.",
                    exception.getMessage() + "\n\nCheck config.properties and try again.");
        } else {
            LOGGER.log(Level.SEVERE, "Unexpected error during the sign-in", exception);
            showAlert(AlertType.ERROR, "An unexpected error occurred.", String.valueOf(exception));
        }
    }

    /**
     * Shows an error message under the form and moves the focus to the field to fix.
     *
     * @param message the message to show
     * @param field   the field that contains the wrong value
     */
    private void showError(String message, Control field) {
        errorLabel.setText(message);
        field.requestFocus();
    }

    /**
     * Hides the error message.
     */
    private void clearError() {
        errorLabel.setText("");
    }

    /**
     * Shows a dialog over the sign-in window and waits until the user closes it.
     *
     * @param type    type of dialog, which sets its icon and buttons
     * @param header  short text shown in the header of the dialog
     * @param content detailed text of the dialog
     */
    private void showAlert(AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.initOwner(stage);
        alert.setTitle("Hello World MVC");
        alert.setHeaderText(header);
        alert.setContentText(content);
        // Let the dialog grow so that long messages are not cut
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    /**
     * Prevents a text field from holding more than the given number of characters: a
     * change that makes the text longer is undone.
     *
     * <p>A listener is used instead of a {@link javafx.scene.control.TextFormatter}
     * because a field with a formatter consumes the Esc key, and then the Exit button,
     * which is the cancel button of the window, would not respond to it.
     *
     * @param field     the text field to limit
     * @param maxLength the maximum number of characters
     */
    private static void limitLength(TextInputControl field, int maxLength) {
        field.textProperty().addListener((observable, oldText, newText) -> {
            if (newText != null && newText.length() > maxLength) {
                field.setText(oldText);
            }
        });
    }
}
