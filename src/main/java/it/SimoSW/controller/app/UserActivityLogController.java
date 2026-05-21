package it.SimoSW.controller.app;

import it.SimoSW.model.UserActivityActionType;
import it.SimoSW.model.UserActivityEntityType;
import it.SimoSW.model.UserActivityLog;
import it.SimoSW.model.dao.UserActivityLogDAO;

import java.util.List;

public class UserActivityLogController {
    private final UserActivityLogDAO userActivityLogDAO;

    public UserActivityLogController(UserActivityLogDAO userActivityLogDAO) {
        this.userActivityLogDAO = userActivityLogDAO;
    }

    public void logAction(String username,
                          String userRole,
                          UserActivityActionType actionType,
                          UserActivityEntityType entityType,
                          String entityId,
                          String description) {
        UserActivityLog log = new UserActivityLog(
                username,
                userRole,
                actionType.name(),
                entityType.name(),
                entityId,
                description
        );
        userActivityLogDAO.save(log);
    }

    public List<UserActivityLog> getRecentLogs(int limit) {
        return userActivityLogDAO.findRecent(limit);
    }
}
