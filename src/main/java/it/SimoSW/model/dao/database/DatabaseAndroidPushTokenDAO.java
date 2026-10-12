package it.SimoSW.model.dao.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseAndroidPushTokenDAO {
    private final ConnectionFactory connectionFactory = ConnectionFactory.getInstance();

    public void register(long userId, String token) {
        String sql = "INSERT INTO android_push_tokens (token, user_id) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE user_id = VALUES(user_id), updated_at = CURRENT_TIMESTAMP";
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Impossibile registrare il dispositivo Android", ex);
        }
    }

    public void deleteToken(String token) {
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM android_push_tokens WHERE token = ?")) {
            stmt.setString(1, token);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Impossibile rimuovere il dispositivo Android", ex);
        }
    }

    public void deleteUserTokens(long userId) {
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM android_push_tokens WHERE user_id = ?")) {
            stmt.setLong(1, userId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Impossibile rimuovere i dispositivi Android", ex);
        }
    }

    public List<String> findAdminTokens() {
        String sql = "SELECT p.token FROM android_push_tokens p "
                + "JOIN users u ON u.id = p.user_id WHERE u.role = 'ADMIN'";
        List<String> tokens = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                tokens.add(rs.getString(1));
            }
            return tokens;
        } catch (SQLException ex) {
            throw new RuntimeException("Impossibile leggere i dispositivi amministratore", ex);
        }
    }
}
