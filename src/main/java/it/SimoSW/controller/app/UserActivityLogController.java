package it.SimoSW.controller.app;

import it.SimoSW.model.UserActivityActionType;
import it.SimoSW.model.UserActivityEntityType;
import it.SimoSW.model.UserActivityLog;
import it.SimoSW.model.dao.UserActivityLogDAO;

import java.time.LocalDate;
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

    public List<UserActivityLog> getLogsByUsername(String username, int limit) {
        if (username == null || username.isBlank()) {
            return getRecentLogs(limit);
        }
        return userActivityLogDAO.findByUsername(username.trim(), limit);
    }

    public List<UserActivityLog> getLogsByDate(LocalDate date, int limit) {
        return userActivityLogDAO.findByDate(date, limit);
    }

    public List<UserActivityLog> getLogsByUsernameAndDate(String username, LocalDate date, int limit) {
        if (username == null || username.isBlank()) {
            return getLogsByDate(date, limit);
        }
        return userActivityLogDAO.findByUsernameAndDate(username.trim(), date, limit);
    }
}
