package tartanga.dami2.din.helloworldmvc.dao;

import java.util.Arrays;
import tartanga.dami2.din.helloworldmvc.exception.ConfigurationException;

/**
 * Lists the available implementations of the data access layer. One of them is
 * selected on each run with the {@code dao.implementation} key of
 * {@code config.properties}.
 *
 * @author Aritz Navarro
 * @author Brayan Romero
 * @author Ekaitz Rivero
 * @version 1.0.0
 */
public enum DaoType {

    /** Implementation that reads the users from a MySQL database through JDBC. */
    DATABASE,

    /** Implementation that reads the users from a properties file. */
    FILE;

    /**
     * Returns the type whose name matches the given text, ignoring case and
     * surrounding spaces.
     *
     * @param text the text to convert, for example {@code "file"}
     * @return the matching type
     * @throws ConfigurationException if the text is {@code null} or does not match any type
     */
    public static DaoType parse(String text) throws ConfigurationException {
        if (text != null) {
            for (DaoType type : values()) {
                if (type.name().equalsIgnoreCase(text.trim())) {
                    return type;
                }
            }
        }
        throw new ConfigurationException("Unknown data access implementation \"" + text
                + "\". Valid values: " + Arrays.toString(values()) + ".");
    }
}
