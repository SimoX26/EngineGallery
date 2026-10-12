package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.AndroidPushService;
import it.SimoSW.model.User;
import it.SimoSW.model.UserRole;
import it.SimoSW.util.bootstrap.ApplicationInitializer;
import it.SimoSW.util.security.CsrfTokenUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/android-push/register")
public class AndroidPushRegistrationServlet extends HttpServlet {
    private AndroidPushService pushService;

    @Override
    public void init() {
        ApplicationInitializer initializer =
                (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        pushService = initializer.getAndroidPushService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = loggedUser(request);
        if (user == null || user.getRole() != UserRole.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        response.setContentType("text/plain; charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(CsrfTokenUtil.ensureToken(request));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = loggedUser(request);
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String token = request.getParameter("token");
        if (token == null || token.length() < 20 || token.length() > 512
                || !token.chars().allMatch(ch -> ch >= 33 && ch <= 126)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            if (user.getRole() == UserRole.ADMIN) {
                pushService.register(user.getId(), token);
            } else {
                pushService.deleteToken(token);
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (RuntimeException ex) {
            getServletContext().log("Registrazione notifiche Android fallita", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private User loggedUser(HttpServletRequest request) {
        Object value = request.getSession(false) != null
                ? request.getSession(false).getAttribute("loggedUser") : null;
        return value instanceof User ? (User) value : null;
    }
}
