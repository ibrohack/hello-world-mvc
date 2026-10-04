package tartanga.dami2.din.helloworldmvc.exception;

/**
 * Signals that no user matches the login and password typed in the sign-in window.
 *
 * <p>The same exception is used when the login does not exist and when the password
 * is wrong, so that the application does not reveal which logins exist.
 *
 * @author Aritz Navarro
 * @author Brayan Romero
 * @author Ekaitz Rivero
 * @version 1.0.0
 */
public class InvalidCredentialsException extends Exception {

    /** Version identifier used by Java serialization. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with the given detail message.
     *
     * @param message description of the problem
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
