package it.SimoSW.model.dao.database;

import it.SimoSW.model.EngineStatus;
import it.SimoSW.model.Image;
import it.SimoSW.model.dao.ImageDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseImageDAO implements ImageDAO {

    private final ConnectionFactory connectionFactory =
            ConnectionFactory.getInstance();

    @Override
    public void save(Image image) {
        String sql = """
            INSERT INTO images (file_path, folder_id)
            VALUES (?, ?)
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, image.getFilePath());
            ps.setLong(2, image.getFolderId());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    image.setId(rs.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error saving image", e);
        }
    }

    @Override
    public void delete(long imageId) {
        String sql = "DELETE FROM images WHERE id = ?";

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, imageId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting image", e);
        }
    }

    @Override
    public Image findById(long imageId) {
        String sql = """
            SELECT id, file_path, folder_id, upload_date
            FROM images
            WHERE id = ?
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, imageId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRowToImage(rs);
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding image by id", e);
        }
    }

    @Override
    public List<Image> findByFolder(long folderId) {
        String sql = """
            SELECT id, file_path, folder_id, upload_date
            FROM images
            WHERE folder_id = ?
        """;

        List<Image> images = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, folderId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                images.add(mapRowToImage(rs));
            }

            return images;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding images by folder", e);
        }
    }

    @Override
    public List<Image> findByClientName(String clientName) {
        String sql = """
            SELECT i.id, i.file_path, i.folder_id, i.upload_date
            FROM images i
            JOIN image_metadata m ON i.id = m.image_id
            WHERE m.client_name LIKE ?
        """;

        return search(sql, "%" + clientName + "%");
    }

    @Override
    public List<Image> findByEngineCode(String engineCode) {
        String sql = """
            SELECT i.id, i.file_path, i.folder_id, i.upload_date
            FROM images i
            JOIN image_metadata m ON i.id = m.image_id
            WHERE m.engine_code = ?
        """;

        return search(sql, engineCode);
    }

    @Override
    public List<Image> findByMetadataKeyword(String keyword) {
        String sql = """
            SELECT i.id, i.file_path, i.folder_id, i.upload_date
            FROM images i
            JOIN image_metadata m ON i.id = m.image_id
            WHERE MATCH(m.description) AGAINST (?)
        """;

        return search(sql, keyword);
    }

    @Override
    public List<Image> findByEngineStatus(EngineStatus status) {
        String sql = """
            SELECT i.id, i.file_path, i.folder_id, i.upload_date
            FROM images i
            JOIN image_metadata m ON i.id = m.image_id
            WHERE m.engine_status = ?
        """;

        return search(sql, status.name());
    }

    // =========================
    // Metodi di supporto
    // =========================

    private List<Image> search(String sql, String param) {
        List<Image> images = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, param);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                images.add(mapRowToImage(rs));
            }

            return images;

        } catch (SQLException e) {
            throw new RuntimeException("Error executing image search", e);
        }
    }

    private Image mapRowToImage(ResultSet rs) throws SQLException {
        Image image = new Image();
        image.setId(rs.getLong("id"));
        image.setFilePath(rs.getString("file_path"));
        image.setFolderId(rs.getLong("folder_id"));
        image.setUploadDate(
                rs.getTimestamp("upload_date").toInstant()
        );
        return image;
    }
}
