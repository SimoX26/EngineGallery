package it.SimoSW.model.dao;

import it.SimoSW.model.UserActivityLog;

import java.time.LocalDate;
import java.util.List;

public interface UserActivityLogDAO {
    void save(UserActivityLog log);

    List<UserActivityLog> findRecent(int limit);

    List<UserActivityLog> findByUsername(String username, int limit);

    List<UserActivityLog> findByDate(LocalDate date, int limit);

    List<UserActivityLog> findByUsernameAndDate(String username, LocalDate date, int limit);
}
