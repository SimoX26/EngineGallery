package it.SimoSW.model.dao.database;

import it.SimoSW.model.Folder;
import it.SimoSW.model.dao.FolderDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseFolderDAO implements FolderDAO {

    private final ConnectionFactory connectionFactory =
            ConnectionFactory.getInstance();

    @Override
    public void save(Folder folder) {
        String sql = """
            INSERT INTO folders (name, parent_id)
            VALUES (?, ?)
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, folder.getName());

            if (folder.getParentId() != null) {
                ps.setLong(2, folder.getParentId());
            } else {
                ps.setNull(2, Types.BIGINT);
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    folder.setId(rs.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error saving folder", e);
        }
    }

    @Override
    public void delete(long folderId) {
        String sql = "DELETE FROM folders WHERE id = ?";

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, folderId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting folder", e);
        }
    }

    @Override
    public Folder findById(long folderId) {
        String sql = """
            SELECT id, name, parent_id
            FROM folders
            WHERE id = ?
        """;

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, folderId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRowToFolder(rs);
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding folder by id", e);
        }
    }

    @Override
    public List<Folder> findSubfolders(long parentFolderId) {
        String sql = """
            SELECT id, name, parent_id
            FROM folders
            WHERE parent_id = ?
        """;

        List<Folder> folders = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, parentFolderId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                folders.add(mapRowToFolder(rs));
            }

            return folders;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding subfolders", e);
        }
    }

    // =========================
    // Metodo di supporto
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
}
