/**
 * Hello World MVC: a JavaFX application in which users sign in and see their data.
 *
 * <p>The application follows a two-layer architecture: the user interface layer
 * (FXML views and their controllers) and the data access layer (DAO). The data access
 * implementation, a properties file or a MySQL database, is selected on each run
 * through the {@code config.properties} file.
 *
 * @author Aritz Navarro
 * @author Brayan Romero
 * @author Ekaitz Rivero
 * @version 1.0.0
 */
module tartanga.dami2.din.helloworldmvc {
    // Transitive because the exported App class exposes JavaFX types (Application, Stage)
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.logging;

    // JavaFX creates the App class, so its package must be accessible
    exports tartanga.dami2.din.helloworldmvc;
    // FXMLLoader creates the controllers and injects their private @FXML fields by reflection
    opens tartanga.dami2.din.helloworldmvc.controller to javafx.fxml;
}
