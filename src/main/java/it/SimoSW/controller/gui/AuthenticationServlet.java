package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.AuthenticationController;
import it.SimoSW.model.User;
import it.SimoSW.util.bootstrap.ApplicationInitializer;
import it.SimoSW.util.security.CookieSecurityUtil;
import it.SimoSW.util.security.LoginRateLimiter;
import it.SimoSW.util.security.RememberMeTokenUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/auth")
public class AuthenticationServlet extends HttpServlet {

    private AuthenticationController authenticationController;
    private final LoginRateLimiter loginRateLimiter = new LoginRateLimiter();
    private static final String COOKIE_REMEMBER = "remember_user_id";
    private static final int COOKIE_MAX_AGE = 60 * 60 * 24 * 30;
    private static final int HTTP_TOO_MANY_REQUESTS = 429;

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
                removePushTokens(session);
                session.invalidate();
            }
            CookieSecurityUtil.clearCookie(response, request, COOKIE_REMEMBER);
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

        if (!loginRateLimiter.isAllowed(username)) {
            response.setStatus(HTTP_TOO_MANY_REQUESTS);
            request.setAttribute("error", "Troppi tentativi di accesso. Riprova tra qualche minuto.");
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
            return;
        }

        Optional<User> user = authenticationController.login(username, password);

        if (user.isPresent()) {
            loginRateLimiter.reset(username);
            HttpSession previousSession = request.getSession(false);
            if (previousSession != null) {
                removePushTokens(previousSession);
                previousSession.invalidate();
            }
            HttpSession session = request.getSession(true);
            session.setAttribute("loggedUser", user.get());

            String rememberToken = RememberMeTokenUtil.createToken(user.get().getId(), COOKIE_MAX_AGE);
            CookieSecurityUtil.setLaxCookie(
                    response,
                    request,
                    COOKIE_REMEMBER,
                    rememberToken,
                    COOKIE_MAX_AGE,
                    true
            );
            response.sendRedirect(request.getContextPath() + "/home");
        } else {
            loginRateLimiter.recordFailure(username);
            // Credenziali errate
            request.setAttribute("error", "Credenziali non valide");
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
        }
    }

    private void removePushTokens(HttpSession session) {
        Object loggedUser = session.getAttribute("loggedUser");
        if (!(loggedUser instanceof User)) {
            return;
        }
        try {
            ApplicationInitializer initializer =
                    (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
            initializer.getAndroidPushService().deleteUserTokens(((User) loggedUser).getId());
        } catch (RuntimeException ex) {
            getServletContext().log("Rimozione notifiche Android al cambio utente fallita", ex);
        }
    }
}
