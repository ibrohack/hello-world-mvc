package tartanga.dami2.din.helloworldmvc.util;

import java.util.regex.Pattern;

/**
 * Validates the values typed by the user in the sign-in window before they are sent
 * to the data access layer.
 *
 * @author Aritz Navarro
 * @author Brayan Romero
 * @author Ekaitz Rivero
 * @version 1.0.0
 */
public final class InputValidator {

    /** Minimum number of characters of a login. */
    public static final int LOGIN_MIN_LENGTH = 3;

    /** Maximum number of characters of a login. */
    public static final int LOGIN_MAX_LENGTH = 30;

    /** Maximum number of characters of a password. */
    public static final int PASSWORD_MAX_LENGTH = 64;

    /** Pattern of a valid login: letters, digits, dots, underscores and hyphens. */
    private static final Pattern LOGIN_PATTERN =
            Pattern.compile("[A-Za-z0-9._-]{" + LOGIN_MIN_LENGTH + "," + LOGIN_MAX_LENGTH + "}");

    /**
     * Prevents the creation of instances: this class only has static members.
     */
    private InputValidator() {
    }

    /**
     * Checks whether a login has a valid format.
     *
     * @param login the login to check; it may be {@code null}
     * @return {@code true} if the login has between {@value #LOGIN_MIN_LENGTH} and
     *         {@value #LOGIN_MAX_LENGTH} characters and only contains letters, digits,
     *         dots, underscores and hyphens
     */
    public static boolean isValidLogin(String login) {
        return login != null && LOGIN_PATTERN.matcher(login).matches();
    }

    /**
     * Checks whether a password has a valid length.
     *
     * @param password the password to check; it may be {@code null}
     * @return {@code true} if the password is not empty and has at most
     *         {@value #PASSWORD_MAX_LENGTH} characters
     */
    public static boolean isValidPassword(String password) {
        return password != null && !password.isEmpty() && password.length() <= PASSWORD_MAX_LENGTH;
    }
}
