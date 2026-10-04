package tartanga.dami2.din.helloworldmvc.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Creates and checks salted password hashes with the PBKDF2 algorithm
 * (PBKDF2WithHmacSHA256), so that passwords are never stored in plain text.
 *
 * <p>A hash is stored as a single text with four fields separated by {@code $}:
 * <pre>pbkdf2_sha256$iterations$salt$hash</pre>
 * where the salt and the hash are encoded in Base64. Keeping the iteration count and
 * the salt next to the hash allows the cost to be raised in the future without
 * invalidating the existing hashes.
 *
 * <p>The class can also be run from the command line to get the hash of a new
 * password, for example with {@code mvn compile exec:java -Dexec.args=MyPassword}.
 *
 * @author Aritz Navarro
 * @author Brayan Romero
 * @author Ekaitz Rivero
 * @version 1.0.0
 */
public final class PasswordHasher {

    /** Identifier of the hash format, stored as the first field of every hash. */
    private static final String FORMAT_ID = "pbkdf2_sha256";

    /** Name of the key derivation algorithm requested from the Java security providers. */
    private static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";

    /** Number of PBKDF2 iterations, as recommended by OWASP for HMAC-SHA256. */
    private static final int ITERATIONS = 600_000;

    /** Length of the random salt, in bytes. */
    private static final int SALT_LENGTH = 16;

    /** Length of the derived key, in bits. */
    private static final int KEY_LENGTH = 256;

    /** Separator between the fields of a stored hash. */
    private static final String SEPARATOR = "$";

    /** Number of fields of a stored hash. */
    private static final int FIELD_COUNT = 4;

    /** Source of cryptographically strong random numbers for the salts. */
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Prevents the creation of instances: this class only has static members.
     */
    private PasswordHasher() {
    }

    /**
     * Returns a new salted hash of the given password.
     *
     * @param password the password, in plain text
     * @return the hash, in the format {@code pbkdf2_sha256$iterations$salt$hash}
     * @throws NullPointerException if {@code password} is {@code null}
     */
    public static String hash(String password) {
        Objects.requireNonNull(password, "password");
        // A new random salt for every hash makes equal passwords produce different hashes
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        byte[] hash = deriveKey(password, salt, ITERATIONS, KEY_LENGTH);
        Base64.Encoder encoder = Base64.getEncoder();
        return String.join(SEPARATOR, FORMAT_ID, String.valueOf(ITERATIONS),
                encoder.encodeToString(salt), encoder.encodeToString(hash));
    }

    /**
     * Checks whether a password matches a stored hash.
     *
     * @param password   the password to check, in plain text
     * @param storedHash the stored hash, as returned by {@link #hash(String)}
     * @return {@code true} if the password matches the hash, {@code false} otherwise
     * @throws IllegalArgumentException if {@code storedHash} is not a valid hash
     * @throws NullPointerException     if {@code password} or {@code storedHash} is {@code null}
     */
    public static boolean verify(String password, String storedHash) {
        Objects.requireNonNull(password, "password");
        Objects.requireNonNull(storedHash, "storedHash");
        String[] fields = storedHash.split(Pattern.quote(SEPARATOR));
        if (fields.length != FIELD_COUNT || !FORMAT_ID.equals(fields[0])) {
            throw new IllegalArgumentException("Unsupported password hash format");
        }
        try {
            int iterations = Integer.parseInt(fields[1]);
            byte[] salt = Base64.getDecoder().decode(fields[2]);
            byte[] expectedHash = Base64.getDecoder().decode(fields[3]);
            // Hash the candidate password with the same salt, cost and length as the stored one
            byte[] actualHash = deriveKey(password, salt, iterations, expectedHash.length * Byte.SIZE);
            // Constant-time comparison: the time taken does not reveal how many bytes match
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException e) {
            // Thrown for a non-numeric iteration count, invalid Base64 or non-positive sizes
            throw new IllegalArgumentException("Malformed password hash", e);
        }
    }

    /**
     * Derives a key from a password with PBKDF2.
     *
     * @param password   the password, in plain text
     * @param salt       the salt mixed with the password
     * @param iterations the number of iterations, which sets the cost of the computation
     * @param keyLength  the length of the derived key, in bits
     * @return the bytes of the derived key
     * @throws IllegalArgumentException if {@code iterations} or {@code keyLength} is not positive
     * @throws IllegalStateException    if the Java runtime does not provide the PBKDF2 algorithm
     */
    private static byte[] deriveKey(String password, byte[] salt, int iterations, int keyLength) {
        char[] passwordChars = password.toCharArray();
        PBEKeySpec keySpec = new PBEKeySpec(passwordChars, salt, iterations, keyLength);
        try {
            return SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM).generateSecret(keySpec).getEncoded();
        } catch (GeneralSecurityException e) {
            // The SunJCE provider included in every JDK supports this algorithm,
            // so this only happens with a broken Java installation
            throw new IllegalStateException(KEY_DERIVATION_ALGORITHM + " is not available", e);
        } finally {
            // Remove the copies of the password from memory as soon as they are not needed
            keySpec.clearPassword();
            Arrays.fill(passwordChars, '\0');
        }
    }

    /**
     * Prints the hash of the password given as the only command-line argument. The
     * result can be pasted into {@code data/users.properties} or into the database to
     * add a user.
     *
     * @param args command-line arguments: the password to hash
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: PasswordHasher <password>");
            return;
        }
        System.out.println(hash(args[0]));
    }
}
