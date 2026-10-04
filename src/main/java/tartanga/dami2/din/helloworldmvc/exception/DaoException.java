package tartanga.dami2.din.helloworldmvc.exception;

/**
 * Signals that the data store cannot be accessed or contains invalid data, for
 * example when the database is unreachable or the users file cannot be read.
 *
 * <p>It usually wraps the lower-level exception that caused the problem, such as a
 * {@link java.sql.SQLException} or an {@link java.io.IOException}.
 *
 * @author Aritz Navarro
 * @author Brayan Romero
 * @author Ekaitz Rivero
 * @version 1.0.0
 */
public class DaoException extends Exception {

    /** Version identifier used by Java serialization. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with the given detail message.
     *
     * @param message description of the problem
     */
    public DaoException(String message) {
        super(message);
    }

    /**
     * Creates an exception with the given detail message and cause.
     *
     * @param message description of the problem
     * @param cause   lower-level exception that caused this one
     */
    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
