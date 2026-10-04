package tartanga.dami2.din.helloworldmvc.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests of {@link InputValidator}.
 *
 * @author Aritz Navarro
 * @author Brayan Romero
 * @author Ekaitz Rivero
 * @version 1.0.0
 */
class InputValidatorTest {

    /**
     * Checks that logins with an allowed length and allowed characters are accepted.
     *
     * @param login the login to check
     */
    @ParameterizedTest
    @ValueSource(strings = {"abc", "demo", "John.Doe", "user_01", "first-last",
        "abcdefghijklmnopqrstuvwxyz1234"})
    void isValidLoginAcceptsValidLogins(String login) {
        assertTrue(InputValidator.isValidLogin(login));
    }

    /**
     * Checks that missing, too short, too long or badly formed logins are rejected.
     *
     * @param login the login to check
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"ab", "abcdefghijklmnopqrstuvwxyz12345", "with space", "demo!", "ñandú"})
    void isValidLoginRejectsInvalidLogins(String login) {
        assertFalse(InputValidator.isValidLogin(login));
    }

    /**
     * Checks that passwords between 1 and 64 characters are accepted.
     *
     * @param password the password to check
     */
    @ParameterizedTest
    @ValueSource(strings = {"x", "Secret1234", "a password with spaces",
        "0123456789012345678901234567890123456789012345678901234567890123"})
    void isValidPasswordAcceptsValidPasswords(String password) {
        assertTrue(InputValidator.isValidPassword(password));
    }

    /**
     * Checks that missing, empty or too long passwords are rejected.
     *
     * @param password the password to check
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"01234567890123456789012345678901234567890123456789012345678901234"})
    void isValidPasswordRejectsInvalidPasswords(String password) {
        assertFalse(InputValidator.isValidPassword(password));
    }
}
