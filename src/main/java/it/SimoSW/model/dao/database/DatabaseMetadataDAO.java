package it.SimoSW.model.dao.database;

import it.SimoSW.model.EngineStatus;
import it.SimoSW.model.Metadata;
import it.SimoSW.model.dao.MetadataDAO;

import java.sql.*;

public class DatabaseMetadataDAO implements MetadataDAO {

    private final ConnectionFactory connectionFactory =
            ConnectionFactory.getInstance();

    @Override
    public void save(Metadata metadata) {
        String sql = """
            INSERT INTO image_metadata
            (image_id, client_name, engine_code, engine_status, description)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, metadata.getImageId());
            ps.setString(2, metadata.getClientName());
            ps.setString(3, metadata.getEngineCode());
            ps.setString(4, metadata.getEngineStatus().name());
            ps.setString(5, metadata.getDescription());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error saving metadata", e);
        }
    }

    @Override
    public void update(Metadata metadata) {
        String sql = """
            UPDATE image_metadata
            SET client_name = ?,
                engine_code = ?,
                engine_status = ?,
                description = ?
            WHERE image_id = ?
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, metadata.getClientName());
            ps.setString(2, metadata.getEngineCode());
            ps.setString(3, metadata.getEngineStatus().name());
            ps.setString(4, metadata.getDescription());
            ps.setLong(5, metadata.getImageId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating metadata", e);
        }
    }

    @Override
    public Metadata findByImageId(long imageId) {
        String sql = """
            SELECT image_id, client_name, engine_code,
                   engine_status, description
            FROM image_metadata
            WHERE image_id = ?
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, imageId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRowToMetadata(rs);
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding metadata by image id", e);
        }
    }

    // =========================
    // Metodo di supporto
    // =========================

    private Metadata mapRowToMetadata(ResultSet rs) throws SQLException {
        Metadata metadata = new Metadata();
        metadata.setImageId(rs.getLong("image_id"));
        metadata.setClientName(rs.getString("client_name"));
        metadata.setEngineCode(rs.getString("engine_code"));
        metadata.setEngineStatus(
                EngineStatus.valueOf(rs.getString("engine_status"))
        );
        metadata.setDescription(rs.getString("description"));
        return metadata;
    }
}
