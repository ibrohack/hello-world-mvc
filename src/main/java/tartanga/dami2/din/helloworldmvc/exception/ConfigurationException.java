package tartanga.dami2.din.helloworldmvc.exception;

/**
 * Signals that the configuration file of the application is missing, cannot be read
 * or contains invalid values.
 *
 * <p>The message explains what is wrong and how to fix it, because it is shown to the
 * user when the application cannot start.
 *
 * @author Aritz Navarro
 * @author Brayan Romero
 * @author Ekaitz Rivero
 * @version 1.0.0
 */
public class ConfigurationException extends Exception {

    /** Version identifier used by Java serialization. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with the given detail message.
     *
     * @param message description of the problem and how to fix it
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * Creates an exception with the given detail message and cause.
     *
     * @param message description of the problem and how to fix it
     * @param cause   lower-level exception that caused this one
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
