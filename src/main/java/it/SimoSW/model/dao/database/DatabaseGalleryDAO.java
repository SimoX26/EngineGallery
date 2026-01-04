package it.SimoSW.model.dao.database;

import it.SimoSW.model.Folder;
import it.SimoSW.model.Image;
import it.SimoSW.model.dao.GalleryDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseGalleryDAO implements GalleryDAO {

    private final ConnectionFactory connectionFactory =
            ConnectionFactory.getInstance();

    @Override
    public List<Folder> loadRootFolders() {
        String sql = """
            SELECT id, name, parent_id
            FROM folders
            WHERE parent_id IS NULL
        """;

        List<Folder> folders = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                folders.add(mapRowToFolder(rs));
            }

            return folders;

        } catch (SQLException e) {
            throw new RuntimeException("Error loading root folders", e);
        }
    }

    @Override
    public List<Image> loadImagesInFolder(long folderId) {
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
            throw new RuntimeException("Error loading images in folder", e);
        }
    }

    // =========================
    // Metodi di supporto
    // =========================

    private Folder mapRowToFolder(ResultSet rs) throws SQLException {
        Folder folder = new Folder();
        folder.setId(rs.getLong("id"));
        folder.setName(rs.getString("name"));

        long parentId = rs.getLong("parent_id");
        if (!rs.wasNull()) {
            folder.setParentId(parentId);
        } else {
            folder.setParentId(null);
        }

        return folder;
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
