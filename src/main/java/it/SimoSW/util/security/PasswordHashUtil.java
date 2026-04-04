package it.SimoSW.util.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordHashUtil {

    private static final String ALGO = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder B64 = Base64.getEncoder();
    private static final Base64.Decoder B64_DECODER = Base64.getDecoder();

    private PasswordHashUtil() {
    }

    public static String hash(String plainPassword) {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        byte[] derived = derive(plainPassword, salt, ITERATIONS, KEY_LENGTH);
        return "pbkdf2$" + ITERATIONS + "$" + B64.encodeToString(salt) + "$" + B64.encodeToString(derived);
    }

    public static boolean verify(String plainPassword, String storedHash) {
        if (storedHash == null || storedHash.isBlank()) {
            return false;
        }
        if (storedHash.startsWith("pbkdf2$")) {
            return verifyPbkdf2(plainPassword, storedHash);
        }
        return verifyLegacySha256(plainPassword, storedHash);
    }

    public static boolean isLegacyHash(String storedHash) {
        return storedHash != null
                && !storedHash.isBlank()
                && !storedHash.startsWith("pbkdf2$");
    }

    private static boolean verifyPbkdf2(String plainPassword, String storedHash) {
        String[] parts = storedHash.split("\\$");
        if (parts.length != 4) {
            return false;
        }
        int iterations;
        try {
            iterations = Integer.parseInt(parts[1]);
        } catch (NumberFormatException ex) {
            return false;
        }

        byte[] salt;
        byte[] expected;
        try {
            salt = B64_DECODER.decode(parts[2]);
            expected = B64_DECODER.decode(parts[3]);
        } catch (IllegalArgumentException ex) {
            return false;
        }

        byte[] actual = derive(plainPassword, salt, iterations, expected.length * 8);
        return MessageDigest.isEqual(expected, actual);
    }

    private static boolean verifyLegacySha256(String plainPassword, String storedHash) {
        String computed = sha256Hex(plainPassword);
        return MessageDigest.isEqual(
                computed.getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static byte[] derive(String plainPassword, byte[] salt, int iterations, int keyLength) {
        try {
            PBEKeySpec spec = new PBEKeySpec(plainPassword.toCharArray(), salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGO);
            return skf.generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Password hashing algorithm not available", ex);
        }
    }

    private static String sha256Hex(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
