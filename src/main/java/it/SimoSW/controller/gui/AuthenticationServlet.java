package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.AuthenticationController;
import it.SimoSW.model.User;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/auth")
public class AuthenticationServlet extends HttpServlet {

    private AuthenticationController authenticationController;
    private static final String COOKIE_REMEMBER = "remember_user_id";
    private static final int COOKIE_MAX_AGE = 60 * 60 * 24 * 30;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.authenticationController = initializer.getAuthenticationController();
    }


    /* =========================
       GET: login / logout
       ========================= */

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String action = request.getParameter("action");

        // Logout
        if ("logout".equals(action)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            clearCookie(response, request, COOKIE_REMEMBER);
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        // Mostra pagina di login
        request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
    }


    /* =========================
       POST: login
       ========================= */

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Optional<User> user = authenticationController.login(username, password);

        if (user.isPresent()) {
            HttpSession session = request.getSession(true);
            session.setAttribute("loggedUser", user.get());

            setCookie(response, request, COOKIE_REMEMBER, Long.toString(user.get().getId()));
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } else {
            // Credenziali errate
            request.setAttribute("error", "Credenziali non valide");
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
        }
    }

    private void setCookie(HttpServletResponse response, HttpServletRequest request,
                           String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setMaxAge(COOKIE_MAX_AGE);
        cookie.setPath(cookiePath(request));
        response.addCookie(cookie);
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
