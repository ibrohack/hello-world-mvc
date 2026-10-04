/**
 * Contains the checked exceptions shared by the layers of the application.
 *
 * <p>The data access layer translates technology-specific errors, such as
 * {@link java.sql.SQLException} or {@link java.io.IOException}, into these exceptions,
 * so that the user interface layer does not depend on how the data is stored.
 *
 * @author Aritz Navarro
 * @author Brayan Romero
 * @author Ekaitz Rivero
 * @version 1.0.0
 */
package tartanga.dami2.din.helloworldmvc.exception;
