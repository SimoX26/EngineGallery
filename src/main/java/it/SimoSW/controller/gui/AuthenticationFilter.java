package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.AuthenticationController;
import it.SimoSW.model.User;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Optional;

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

        if (isPublicPath(path)) {
            if (isHomePath(path) || "/auth".equals(path)) {
                ensureAutoLogin(req, res, contextPath);
                if (isLoggedIn(req)) {
                    res.sendRedirect(contextPath + "/dashboard");
                    return;
                }
            }
            chain.doFilter(request, response);
            return;
        }

        if (!isLoggedIn(req)) {
            ensureAutoLogin(req, res, contextPath);
        }

        if (!isLoggedIn(req)) {
            res.sendRedirect(contextPath + "/auth");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isLoggedIn(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session != null && session.getAttribute("loggedUser") != null;
    }

    private void ensureAutoLogin(HttpServletRequest req, HttpServletResponse res, String contextPath) {
        Cookie rememberCookie = findCookie(req, COOKIE_REMEMBER);
        if (rememberCookie == null) {
            return;
        }

        long userId;
        try {
            userId = Long.parseLong(rememberCookie.getValue());
        } catch (NumberFormatException ex) {
            clearCookie(res, COOKIE_REMEMBER, contextPath);
            return;
        }

        Optional<User> userOpt = authenticationController.findById(userId);
        if (userOpt.isEmpty()) {
            clearCookie(res, COOKIE_REMEMBER, contextPath);
            return;
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("loggedUser", userOpt.get());
        setCookie(res, COOKIE_REMEMBER, Long.toString(userId), contextPath, COOKIE_MAX_AGE, req.isSecure());
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

    private void setCookie(HttpServletResponse res, String name, String value,
                           String contextPath, int maxAge, boolean secure) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setMaxAge(maxAge);
        cookie.setPath(cookiePath(contextPath));
        res.addCookie(cookie);
    }

    private void clearCookie(HttpServletResponse res, String name, String contextPath) {
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        cookie.setPath(cookiePath(contextPath));
        res.addCookie(cookie);
    }

    private String cookiePath(String contextPath) {
        return contextPath == null || contextPath.isEmpty() ? "/" : contextPath;
    }
}
