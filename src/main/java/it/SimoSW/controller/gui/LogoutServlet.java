package it.SimoSW.controller.gui;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    private static final String COOKIE_REMEMBER = "remember_user_id";
    private static final String COOKIE_LAST_PATH = "last_path";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Recupera la sessione se esiste (senza crearne una nuova)
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate(); // distrugge completamente la sessione
        }

        clearCookie(response, request, COOKIE_REMEMBER);
        clearCookie(response, request, COOKIE_LAST_PATH);

        // Redirect alla landing page
        response.sendRedirect(request.getContextPath() + "/home");
    }

    /**
     * Opzionale: se qualcuno prova ad accedere via GET,
     * lo trattiamo come un POST per sicurezza.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    private void clearCookie(HttpServletResponse response, HttpServletRequest request, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        cookie.setPath(cookiePath(request));
        response.addCookie(cookie);
    }

    private String cookiePath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return contextPath == null || contextPath.isEmpty() ? "/" : contextPath;
    }
}
