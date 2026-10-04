package tartanga.dami2.din.helloworldmvc.controller;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import tartanga.dami2.din.helloworldmvc.dao.UserDao;
import tartanga.dami2.din.helloworldmvc.model.User;

/**
 * Controller of the user data window, defined in {@code UserDataView.fxml}.
 *
 * <p>It greets the signed-in user, shows their data and the data store it was read
 * from, and lets the user sign out or exit the application.
 *
 * @author Aritz Navarro
 * @author Brayan Romero
 * @author Ekaitz Rivero
 * @version 1.0.0
 */
public class UserDataController {

    /** Location of the FXML view controlled by this class, among the application resources. */
    public static final String VIEW = "/tartanga/dami2/din/helloworldmvc/view/UserDataView.fxml";

    /** Format used to show the date of birth, for example {@code 15/01/2000}. */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Logger of this class. */
    private static final Logger LOGGER = Logger.getLogger(UserDataController.class.getName());

    /** Label that greets the user by their first name. */
    @FXML
    private Label greetingLabel;

    /** Label that shows the login of the user. */
    @FXML
    private Label loginLabel;

    /** Label that shows the first name of the user. */
    @FXML
    private Label firstNameLabel;

    /** Label that shows the last name of the user. */
    @FXML
    private Label lastNameLabel;

    /** Label that shows the email address of the user. */
    @FXML
    private Label emailLabel;

    /** Label that shows the date of birth of the user. */
    @FXML
    private Label birthDateLabel;

    /** Label that shows the data store the user was read from. */
    @FXML
    private Label dataSourceLabel;

    /** Window managed by this controller. */
    private Stage stage;

    /** Data access object, passed on to the sign-in window when the user signs out. */
    private UserDao userDao;

    /** Signed-in user whose data is shown. */
    private User user;

    /**
     * Creates the controller. {@link javafx.fxml.FXMLLoader} calls this constructor when
     * it loads the view.
     */
    public UserDataController() {
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
     * Sets the data access object, which is passed on to the sign-in window when the
     * user signs out.
     *
     * @param userDao the data access object
     */
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Sets the signed-in user whose data is shown.
     *
     * @param user the signed-in user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Fills the view with the data of the user and shows the window.
     *
     * @param root root node of the loaded view
     */
    public void initStage(Parent root) {
        showUserData();
        stage.setScene(new Scene(root));
        stage.setTitle("Hello World MVC - " + user.getFullName());
        stage.setResizable(false);
        // The close button of the window asks for confirmation, like the Exit button
        stage.setOnCloseRequest(this::handleCloseRequest);
        stage.show();
    }

    /**
     * Copies the data of the user, and the name of the data store, to the labels of
     * the view.
     */
    private void showUserData() {
        greetingLabel.setText("Hello, " + user.getFirstName() + "!");
        loginLabel.setText(user.getLogin());
        firstNameLabel.setText(user.getFirstName());
        lastNameLabel.setText(user.getLastName());
        emailLabel.setText(user.getEmail());
        birthDateLabel.setText(user.getBirthDate().format(DATE_FORMAT));
        dataSourceLabel.setText(userDao.getDataSourceName());
    }

    /**
     * Handles the sign-out button: opens a new sign-in window and closes this one.
     */
    @FXML
    private void handleSignOutAction() {
        LOGGER.log(Level.INFO, "User {0} signed out", user.getLogin());
        try {
            FXMLLoader loader = new FXMLLoader(LoginController.class.getResource(LoginController.VIEW));
            Parent root = loader.load();
            LoginController controller = loader.getController();
            controller.setStage(new Stage());
            controller.setUserDao(userDao);
            controller.initStage(root);
            // The sign-in window is already open, so closing this one does not end the application
            stage.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "The sign-in window cannot be opened", e);
            showError("The sign-in window cannot be opened.", e.getMessage());
        }
    }

    /**
     * Handles the exit button: closes the application if the user confirms it.
     */
    @FXML
    private void handleExitAction() {
        if (confirmExit()) {
            Platform.exit();
        }
    }

    /**
     * Handles the close button of the window: the window is only closed, ending the
     * application, if the user confirms it.
     *
     * @param event the close request; consuming it keeps the window open
     */
    private void handleCloseRequest(WindowEvent event) {
        if (!confirmExit()) {
            event.consume();
        }
    }

    /**
     * Asks the user to confirm that they want to exit the application.
     *
     * @return {@code true} if the user confirms
     */
    private boolean confirmExit() {
        Alert alert = new Alert(AlertType.CONFIRMATION, "Do you really want to exit Hello World MVC?",
                ButtonType.YES, ButtonType.NO);
        alert.initOwner(stage);
        alert.setTitle("Hello World MVC");
        alert.setHeaderText("Exit");
        Optional<ButtonType> answer = alert.showAndWait();
        // Closing the dialog without choosing counts as "No"
        boolean exit = answer.isPresent() && answer.get() == ButtonType.YES;
        if (exit) {
            LOGGER.log(Level.INFO, "Application closed by user {0}", user.getLogin());
        }
        return exit;
    }

    /**
     * Shows an error dialog over the user data window and waits until the user closes it.
     *
     * @param header  short description of the problem
     * @param content detailed text of the dialog
     */
    private void showError(String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.initOwner(stage);
        alert.setTitle("Hello World MVC");
        alert.setHeaderText(header);
        alert.setContentText(content);
        // Let the dialog grow so that long messages are not cut
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }
}
