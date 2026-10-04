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
// The module is named after the project base package, whose "dami2" component ends in a
// digit; the compiler warns about that naming style, so the warning is suppressed here.
@SuppressWarnings("module")
module tartanga.dami2.din.helloworldmvc {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.logging;
}
