package it.SimoSW.util.navigation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class PostSubmitNavigationGuard {

    private static final String SESSION_KEY = "postSubmitNavigationGuard.blocks";

    private PostSubmitNavigationGuard() {
    }

    public static void blockFormPageOnce(HttpServletRequest request, String formPath, String fallbackPath) {
        if (formPath == null || formPath.isBlank() || fallbackPath == null || fallbackPath.isBlank()) {
            return;
        }

        HttpSession session = request.getSession();
        Map<String, String> blocks = getOrCreateBlocks(session);
        blocks.put(normalizePath(formPath), normalizePath(fallbackPath));
    }

    public static boolean redirectIfBlocked(HttpServletRequest request,
                                            HttpServletResponse response,
                                            String formPath) throws IOException {
        applyNoCacheHeaders(response);

        if (formPath == null || formPath.isBlank()) {
            return false;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        @SuppressWarnings("unchecked")
        Map<String, String> blocks = (Map<String, String>) session.getAttribute(SESSION_KEY);
        if (blocks == null || blocks.isEmpty()) {
            return false;
        }

        String fallbackPath = blocks.remove(normalizePath(formPath));
        if (blocks.isEmpty()) {
            session.removeAttribute(SESSION_KEY);
        }

        if (fallbackPath == null || fallbackPath.isBlank()) {
            return false;
        }

        response.sendRedirect(request.getContextPath() + fallbackPath);
        return true;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> getOrCreateBlocks(HttpSession session) {
        Map<String, String> blocks = (Map<String, String>) session.getAttribute(SESSION_KEY);
        if (blocks == null) {
            blocks = new HashMap<>();
            session.setAttribute(SESSION_KEY, blocks);
        }
        return blocks;
    }

    private static void applyNoCacheHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    private static String normalizePath(String path) {
        String trimmed = path.trim();
        if (!trimmed.startsWith("/")) {
            return "/" + trimmed;
        }
        return trimmed;
    }
}
