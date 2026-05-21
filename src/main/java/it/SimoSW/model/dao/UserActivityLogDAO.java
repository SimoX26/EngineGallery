package it.SimoSW.model.dao;

import it.SimoSW.model.UserActivityLog;

import java.util.List;

public interface UserActivityLogDAO {
    void save(UserActivityLog log);

    List<UserActivityLog> findRecent(int limit);
}
