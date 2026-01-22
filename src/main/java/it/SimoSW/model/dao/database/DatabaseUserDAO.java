package it.SimoSW.model.dao.database;

import it.SimoSW.model.User;
import it.SimoSW.model.UserRole;
import it.SimoSW.model.dao.UserDAO;

import java.sql.*;

public class DatabaseUserDAO implements UserDAO {

    private final ConnectionFactory connectionFactory = ConnectionFactory.getInstance();

    @Override
    public User findByUsername(String username) {
        String sql = """
            SELECT id, username, password_hash, role
            FROM users
            WHERE username = ?
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRowToUser(rs);
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by username", e);
        }
    }

    @Override
    public User findById(long userId) {
        String sql = """
            SELECT id, username, password_hash, role
            FROM users
            WHERE id = ?
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRowToUser(rs);
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by id", e);
        }
    }

    // =========================
    // Metodo di supporto
    // =========================

    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();

        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(UserRole.valueOf(rs.getString("role").toUpperCase()));

        return user;
    }
}
