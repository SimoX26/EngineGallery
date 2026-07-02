package it.SimoSW.model.dao.database;

import it.SimoSW.model.UserActivityLog;
import it.SimoSW.model.dao.UserActivityLogDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUserActivityLogDAO implements UserActivityLogDAO {
    private static final int RECENT_ACTIVITY_DAYS = 10;

    private static final String INSERT_SQL = """
            INSERT INTO user_activity_log
            (username, user_role, action_type, entity_type, entity_id, description)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String FIND_RECENT_SQL = """
            SELECT id, username, user_role, action_type, entity_type, entity_id, description, created_at
            FROM user_activity_log
            WHERE created_at >= ?
            ORDER BY created_at DESC, id DESC
            LIMIT ?
            """;

    private static final String FIND_BY_USERNAME_SQL = """
            SELECT id, username, user_role, action_type, entity_type, entity_id, description, created_at
            FROM user_activity_log
            WHERE LOWER(username) = LOWER(?)
              AND created_at >= ?
            ORDER BY created_at DESC, id DESC
            LIMIT ?
            """;

    private static final String FIND_BY_DATE_SQL = """
            SELECT id, username, user_role, action_type, entity_type, entity_id, description, created_at
            FROM user_activity_log
            WHERE created_at >= ?
              AND created_at >= ? AND created_at < ?
            ORDER BY created_at DESC, id DESC
            LIMIT ?
            """;

    private static final String FIND_BY_USERNAME_AND_DATE_SQL = """
            SELECT id, username, user_role, action_type, entity_type, entity_id, description, created_at
            FROM user_activity_log
            WHERE LOWER(username) = LOWER(?)
              AND created_at >= ?
              AND created_at >= ? AND created_at < ?
            ORDER BY created_at DESC, id DESC
            LIMIT ?
            """;

    private static final String FIND_BY_ENTITY_TYPE_AND_ACTION_TYPE_SQL = """
            SELECT id, username, user_role, action_type, entity_type, entity_id, description, created_at
            FROM user_activity_log
            WHERE entity_type = ?
              AND action_type = ?
            ORDER BY created_at ASC, id ASC
            """;

    @Override
    public void save(UserActivityLog log) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, log.getUsername());
            stmt.setString(2, log.getUserRole());
            stmt.setString(3, log.getActionType());
            stmt.setString(4, log.getEntityType());
            stmt.setString(5, log.getEntityId());
            stmt.setString(6, log.getDescription());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    log.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio del log attività", e);
        }
    }

    @Override
    public List<UserActivityLog> findRecent(int limit) {
        int safeLimit = Math.max(1, limit);
        List<UserActivityLog> logs = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_RECENT_SQL)) {
            stmt.setTimestamp(1, recentActivityCutoff());
            stmt.setInt(2, safeLimit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero log attività", e);
        }
        return logs;
    }

    @Override
    public List<UserActivityLog> findByUsername(String username, int limit) {
        int safeLimit = Math.max(1, limit);
        List<UserActivityLog> logs = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_USERNAME_SQL)) {
            stmt.setString(1, username);
            stmt.setTimestamp(2, recentActivityCutoff());
            stmt.setInt(3, safeLimit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero log attività per utente", e);
        }
        return logs;
    }

    @Override
    public List<UserActivityLog> findByDate(LocalDate date, int limit) {
        int safeLimit = Math.max(1, limit);
        List<UserActivityLog> logs = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_DATE_SQL)) {
            stmt.setTimestamp(1, recentActivityCutoff());
            bindDateRange(stmt, date, 2);
            stmt.setInt(4, safeLimit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero log attività per data", e);
        }
        return logs;
    }

    @Override
    public List<UserActivityLog> findByUsernameAndDate(String username, LocalDate date, int limit) {
        int safeLimit = Math.max(1, limit);
        List<UserActivityLog> logs = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_USERNAME_AND_DATE_SQL)) {
            stmt.setString(1, username);
            stmt.setTimestamp(2, recentActivityCutoff());
            bindDateRange(stmt, date, 3);
            stmt.setInt(5, safeLimit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero log attività per utente e data", e);
        }
        return logs;
    }

    @Override
    public List<UserActivityLog> findByEntityTypeAndActionType(String entityType, String actionType) {
        List<UserActivityLog> logs = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ENTITY_TYPE_AND_ACTION_TYPE_SQL)) {
            stmt.setString(1, entityType);
            stmt.setString(2, actionType);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero log attività per entità e azione", e);
        }
        return logs;
    }

    private static Timestamp recentActivityCutoff() {
        return Timestamp.valueOf(LocalDate.now().minusDays(RECENT_ACTIVITY_DAYS - 1L).atStartOfDay());
    }

    private static void bindDateRange(PreparedStatement stmt, LocalDate date, int startIndex) throws SQLException {
        LocalDate safeDate = date != null ? date : LocalDate.now();
        stmt.setTimestamp(startIndex, Timestamp.valueOf(safeDate.atStartOfDay()));
        stmt.setTimestamp(startIndex + 1, Timestamp.valueOf(safeDate.plusDays(1).atStartOfDay()));
    }

    private static UserActivityLog mapRow(ResultSet rs) throws SQLException {
        UserActivityLog log = new UserActivityLog();
        log.setId(rs.getLong("id"));
        log.setUsername(rs.getString("username"));
        log.setUserRole(rs.getString("user_role"));
        log.setActionType(rs.getString("action_type"));
        log.setEntityType(rs.getString("entity_type"));
        log.setEntityId(rs.getString("entity_id"));
        log.setDescription(rs.getString("description"));
        java.sql.Timestamp ts = rs.getTimestamp("created_at");
        log.setCreatedAt(ts != null ? ts.toLocalDateTime() : (LocalDateTime) null);
        return log;
    }
}
