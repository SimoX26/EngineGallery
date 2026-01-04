package it.SimoSW.model.dao.database;

import it.SimoSW.model.Folder;
import it.SimoSW.model.dao.GalleryDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO per la gestione della struttura delle cartelle della galleria.
 * Le immagini NON sono gestite da questo DAO.
 */
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
    public List<Folder> loadSubFolders(long parentFolderId) {
        String sql = """
            SELECT id, name, parent_id
            FROM folders
            WHERE parent_id = ?
        """;

        List<Folder> folders = new ArrayList<>();

        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, parentFolderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    folders.add(mapRowToFolder(rs));
                }
            }

            return folders;

        } catch (SQLException e) {
            throw new RuntimeException("Error loading subfolders", e);
        }
    }

    /* =========================
       Mapping
       ========================= */

    private Folder mapRowToFolder(ResultSet rs) throws SQLException {
        Folder folder = new Folder();
        folder.setId(rs.getLong("id"));
        folder.setName(rs.getString("name"));

        Long parentId = rs.getObject("parent_id", Long.class);
        folder.setParentId(parentId);

        return folder;
    }
}
