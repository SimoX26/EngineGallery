package it.SimoSW.util.audit;

import it.SimoSW.controller.app.UserActivityLogController;
import it.SimoSW.model.User;
import it.SimoSW.model.UserActivityActionType;
import it.SimoSW.model.UserActivityEntityType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserActivityAuditLogger {
    private static final Logger LOGGER = Logger.getLogger(UserActivityAuditLogger.class.getName());

    private final UserActivityLogController controller;

    public UserActivityAuditLogger(UserActivityLogController controller) {
        this.controller = controller;
    }

    public void logFromRequest(HttpServletRequest request,
                               UserActivityActionType actionType,
                               UserActivityEntityType entityType,
                               String entityId,
                               String description) {
        try {
            HttpSession session = request.getSession(false);
            User user = session != null ? (User) session.getAttribute("loggedUser") : null;
            String username = user != null && user.getUsername() != null && !user.getUsername().isBlank()
                    ? user.getUsername()
                    : "unknown";
            String role = user != null && user.getRole() != null ? user.getRole().name() : null;
            controller.logAction(username, role, actionType, entityType, entityId, description);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Impossibile registrare activity log", ex);
        }
    }
}
