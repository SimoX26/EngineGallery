package it.SimoSW.util.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
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
    private static final String ENV_VAR = "ENGINE_GALLERY_REMEMBER_SECRET";
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

        byte[] ephemeral = new byte[32];
        RANDOM.nextBytes(ephemeral);
        LOGGER.warning(() ->
                "Remember-me secret not configured. Tokens will be invalidated on restart. " +
                        "Set " + SYS_PROP + " or " + ENV_VAR + ".");
        return ephemeral;
    }
}
