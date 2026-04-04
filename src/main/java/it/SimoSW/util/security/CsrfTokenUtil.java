package it.SimoSW.util.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class CsrfTokenUtil {

    public static final String SESSION_ATTR = "csrf_token";
    public static final String PARAM = "csrfToken";

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder B64_URL = Base64.getUrlEncoder().withoutPadding();

    private CsrfTokenUtil() {
    }

    public static String ensureToken(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Object existing = session.getAttribute(SESSION_ATTR);
        if (existing instanceof String && !((String) existing).isBlank()) {
            return (String) existing;
        }
        String generated = generateToken();
        session.setAttribute(SESSION_ATTR, generated);
        return generated;
    }

    public static boolean isValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        Object sessionTokenObj = session.getAttribute(SESSION_ATTR);
        if (!(sessionTokenObj instanceof String)) {
            return false;
        }

        String sessionToken = (String) sessionTokenObj;
        String requestToken = request.getParameter(PARAM);
        if (sessionToken.isBlank() || requestToken == null || requestToken.isBlank()) {
            return false;
        }

        return MessageDigest.isEqual(
                sessionToken.getBytes(StandardCharsets.UTF_8),
                requestToken.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return B64_URL.encodeToString(bytes);
    }
}
