package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.AuthenticationController;
import it.SimoSW.model.User;
import it.SimoSW.util.bootstrap.ApplicationInitializer;
import it.SimoSW.util.security.CookieSecurityUtil;
import it.SimoSW.util.security.CsrfTokenUtil;
import it.SimoSW.util.security.RememberMeTokenUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Optional;
import java.util.OptionalLong;

@WebFilter("/*")
public class AuthenticationFilter implements Filter {

    private static final String COOKIE_REMEMBER = "remember_user_id";
    private static final int COOKIE_MAX_AGE = 60 * 60 * 24 * 30;

    private AuthenticationController authenticationController;

    @Override
    public void init(FilterConfig filterConfig) {
        ServletContext context = filterConfig.getServletContext();
        ApplicationInitializer initializer =
                (ApplicationInitializer) context.getAttribute("appInitializer");
        this.authenticationController = initializer.getAuthenticationController();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String contextPath = req.getContextPath();
        String path = req.getRequestURI().substring(contextPath.length());
        if (path.isEmpty()) {
            path = "/";
        }

        ensureCsrfTokenForPageRequests(req, path);

        if (isPublicPath(path)) {
            if (isHomePath(path) || "/auth".equals(path)) {
                ensureAutoLogin(req, res);
                if (isLoggedIn(req)) {
                    res.sendRedirect(contextPath + "/dashboard");
                    return;
                }
            }
            if (requiresCsrfValidation(req, path) && !CsrfTokenUtil.isValid(req)) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        if (!isLoggedIn(req)) {
            ensureAutoLogin(req, res);
        }

        if (!isLoggedIn(req)) {
            res.sendRedirect(contextPath + "/auth");
            return;
        }

        if (requiresCsrfValidation(req, path) && !CsrfTokenUtil.isValid(req)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isLoggedIn(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session != null && session.getAttribute("loggedUser") != null;
    }

    private void ensureAutoLogin(HttpServletRequest req, HttpServletResponse res) {
        Cookie rememberCookie = findCookie(req, COOKIE_REMEMBER);
        if (rememberCookie == null) {
            return;
        }

        OptionalLong userIdOpt = RememberMeTokenUtil.verifyAndExtractUserId(rememberCookie.getValue());
        if (userIdOpt.isEmpty()) {
            clearCookie(res, req, COOKIE_REMEMBER);
            return;
        }
        long userId = userIdOpt.getAsLong();

        Optional<User> userOpt = authenticationController.findById(userId);
        if (userOpt.isEmpty()) {
            clearCookie(res, req, COOKIE_REMEMBER);
            return;
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("loggedUser", userOpt.get());
        String rotatedToken = RememberMeTokenUtil.createToken(userId, COOKIE_MAX_AGE);
        setCookie(res, req, COOKIE_REMEMBER, rotatedToken, COOKIE_MAX_AGE);
    }

    private boolean isPublicPath(String path) {
        return path.equals("/")
                || path.equals("/home")
                || path.equals("/auth")
                || path.equals("/index.jsp")
                || path.equals("/logout")
                || path.startsWith("/assets/")
                || path.equals("/image/share");
    }

    private boolean isHomePath(String path) {
        return path.equals("/") || path.equals("/home") || path.equals("/index.jsp");
    }

    private Cookie findCookie(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }

    private void setCookie(HttpServletResponse res, HttpServletRequest req, String name, String value, int maxAge) {
        CookieSecurityUtil.setLaxCookie(res, req, name, value, maxAge, true);
    }

    private void clearCookie(HttpServletResponse res, HttpServletRequest req, String name) {
        CookieSecurityUtil.clearCookie(res, req, name);
    }

    private void ensureCsrfTokenForPageRequests(HttpServletRequest req, String path) {
        String method = req.getMethod();
        if ("GET".equalsIgnoreCase(method)
                && !path.startsWith("/assets/")
                && !path.startsWith("/uploads/")) {
            CsrfTokenUtil.ensureToken(req);
        }
    }

    private boolean requiresCsrfValidation(HttpServletRequest req, String path) {
        if (!"POST".equalsIgnoreCase(req.getMethod())) {
            return false;
        }
        return !path.startsWith("/assets/") && !path.startsWith("/uploads/");
    }
}
