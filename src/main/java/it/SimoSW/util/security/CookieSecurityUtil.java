package it.SimoSW.util.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class CookieSecurityUtil {

    private CookieSecurityUtil() {
    }

    public static void setLaxCookie(HttpServletResponse response,
                                    HttpServletRequest request,
                                    String name,
                                    String value,
                                    int maxAgeSeconds,
                                    boolean httpOnly) {
        String path = cookiePath(request);
        boolean secure = request.isSecure();
        StringBuilder header = new StringBuilder();
        header.append(name).append("=").append(value)
                .append("; Max-Age=").append(maxAgeSeconds)
                .append("; Path=").append(path)
                .append("; SameSite=Lax");
        if (httpOnly) {
            header.append("; HttpOnly");
        }
        if (secure) {
            header.append("; Secure");
        }
        response.addHeader("Set-Cookie", header.toString());
    }

    public static void clearCookie(HttpServletResponse response, HttpServletRequest request, String name) {
        String path = cookiePath(request);
        StringBuilder header = new StringBuilder();
        header.append(name).append("=; Max-Age=0; Path=").append(path).append("; SameSite=Lax");
        if (request.isSecure()) {
            header.append("; Secure");
        }
        header.append("; HttpOnly");
        response.addHeader("Set-Cookie", header.toString());
    }

    private static String cookiePath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return (contextPath == null || contextPath.isEmpty()) ? "/" : contextPath;
    }
}
