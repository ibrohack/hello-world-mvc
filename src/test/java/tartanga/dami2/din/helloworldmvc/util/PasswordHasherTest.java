package tartanga.dami2.din.helloworldmvc.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests of {@link PasswordHasher}.
 *
 * @author Aritz Navarro
 * @author Brayan Romero
 * @author Ekaitz Rivero
 * @version 1.0.0
 */
class PasswordHasherTest {

    /** Password used by the tests. */
    private static final String PASSWORD = "Secret1234";

    /**
     * Checks that a hash is accepted for the password it was created from.
     */
    @Test
    void verifyReturnsTrueForTheOriginalPassword() {
        String hash = PasswordHasher.hash(PASSWORD);
        assertTrue(PasswordHasher.verify(PASSWORD, hash));
    }

    /**
     * Checks that a hash is rejected for a different password.
     */
    @Test
    void verifyReturnsFalseForAnotherPassword() {
        String hash = PasswordHasher.hash(PASSWORD);
        assertFalse(PasswordHasher.verify("secret1234", hash));
    }

    /**
     * Checks that a hash has four fields and starts with the format identifier and
     * the iteration count.
     */
    @Test
    void hashUsesTheDocumentedFormat() {
        String hash = PasswordHasher.hash(PASSWORD);
        assertTrue(hash.startsWith("pbkdf2_sha256$600000$"));
        assertEquals(4, hash.split("\\$").length);
    }

    /**
     * Checks that hashing the same password twice gives different results, because
     * each hash uses a new random salt.
     */
    @Test
    void hashUsesADifferentSaltEachTime() {
        assertNotEquals(PasswordHasher.hash(PASSWORD), PasswordHasher.hash(PASSWORD));
    }

    /**
     * Checks that a text that is not a valid hash is rejected with an exception.
     */
    @Test
    void verifyRejectsAMalformedHash() {
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.verify(PASSWORD, "not-a-hash"));
        assertThrows(IllegalArgumentException.class,
                () -> PasswordHasher.verify(PASSWORD, "pbkdf2_sha256$many$salt$hash"));
    }
}
