package it.SimoSW.util.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.OptionalLong;
import java.util.logging.Logger;

public final class RememberMeTokenUtil {

    private static final Logger LOGGER = Logger.getLogger(RememberMeTokenUtil.class.getName());
    private static final String SYS_PROP = "enginegallery.remember.secret";
    private static final String SYS_PROP_FILE = "enginegallery.remember.secret.file";
    private static final String ENV_VAR = "ENGINE_GALLERY_REMEMBER_SECRET";
    private static final String ENV_VAR_FILE = "ENGINE_GALLERY_REMEMBER_SECRET_FILE";
    private static final String HMAC_ALGO = "HmacSHA256";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder B64_URL = Base64.getUrlEncoder().withoutPadding();
    private static final byte[] SECRET = loadSecret();

    private RememberMeTokenUtil() {
    }

    public static String createToken(long userId, int maxAgeSeconds) {
        long expiresAt = Instant.now().getEpochSecond() + Math.max(1, maxAgeSeconds);
        byte[] nonceBytes = new byte[16];
        RANDOM.nextBytes(nonceBytes);
        String nonce = B64_URL.encodeToString(nonceBytes);
        String payload = userId + ":" + expiresAt + ":" + nonce;
        String signature = sign(payload);
        return payload + ":" + signature;
    }

    public static OptionalLong verifyAndExtractUserId(String tokenValue) {
        if (tokenValue == null || tokenValue.isBlank()) {
            return OptionalLong.empty();
        }

        String[] parts = tokenValue.split(":");
        if (parts.length != 4) {
            return OptionalLong.empty();
        }

        long userId;
        long expiresAt;
        try {
            userId = Long.parseLong(parts[0]);
            expiresAt = Long.parseLong(parts[1]);
        } catch (NumberFormatException ex) {
            return OptionalLong.empty();
        }

        if (userId <= 0 || expiresAt < Instant.now().getEpochSecond()) {
            return OptionalLong.empty();
        }

        String payload = parts[0] + ":" + parts[1] + ":" + parts[2];
        String expectedSignature = sign(payload);
        if (!constantTimeEquals(expectedSignature, parts[3])) {
            return OptionalLong.empty();
        }

        return OptionalLong.of(userId);
    }

    private static String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(SECRET, HMAC_ALGO));
            byte[] signatureBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return B64_URL.encodeToString(signatureBytes);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Unable to sign remember-me token", ex);
        }
    }

    private static boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        byte[] leftBytes = left.getBytes(StandardCharsets.UTF_8);
        byte[] rightBytes = right.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(leftBytes, rightBytes);
    }

    private static byte[] loadSecret() {
        String configured = System.getProperty(SYS_PROP);
        if (configured == null || configured.isBlank()) {
            configured = System.getenv(ENV_VAR);
        }
        if (configured != null && !configured.isBlank()) {
            return configured.getBytes(StandardCharsets.UTF_8);
        }

        byte[] secretFromFile = loadOrCreateSecretFromFile();
        if (secretFromFile != null) {
            return secretFromFile;
        }

        byte[] fallbackEphemeral = new byte[32];
        RANDOM.nextBytes(fallbackEphemeral);
        LOGGER.warning(() ->
                "Remember-me secret not configured and cannot be persisted. Tokens may be invalidated on restart. " +
                        "Set " + SYS_PROP + ", " + ENV_VAR + ", " + SYS_PROP_FILE + " or " + ENV_VAR_FILE + ".");
        return fallbackEphemeral;
    }

    private static byte[] loadOrCreateSecretFromFile() {
        Path filePath = resolveSecretFilePath();
        if (filePath == null) {
            return null;
        }

        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            if (Files.exists(filePath)) {
                String existing = Files.readString(filePath, StandardCharsets.UTF_8).trim();
                if (!existing.isBlank()) {
                    return existing.getBytes(StandardCharsets.UTF_8);
                }
            }

            byte[] bytes = new byte[48];
            RANDOM.nextBytes(bytes);
            String generated = B64_URL.encodeToString(bytes);
            Files.writeString(
                    filePath,
                    generated,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
            LOGGER.info(() -> "Created persistent remember-me secret at: " + filePath);
            return generated.getBytes(StandardCharsets.UTF_8);
        } catch (IOException ex) {
            LOGGER.warning(() -> "Unable to read/create remember-me secret file '" + filePath + "': " + ex.getMessage());
            return null;
        }
    }

    private static Path resolveSecretFilePath() {
        String configuredFile = System.getProperty(SYS_PROP_FILE);
        if (configuredFile == null || configuredFile.isBlank()) {
            configuredFile = System.getenv(ENV_VAR_FILE);
        }

        if (configuredFile != null && !configuredFile.isBlank()) {
            return Paths.get(configuredFile.trim()).toAbsolutePath().normalize();
        }

        String userHome = System.getProperty("user.home");
        if (userHome == null || userHome.isBlank()) {
            return null;
        }
        return Paths.get(userHome, ".enginegallery", "remember-secret.key").toAbsolutePath().normalize();
    }
}
