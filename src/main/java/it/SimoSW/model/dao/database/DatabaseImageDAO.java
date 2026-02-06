package it.SimoSW.model.dao.database;

import it.SimoSW.model.Image;
import it.SimoSW.model.dao.ImageDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementazione JDBC di ImageDAO.
 */
public class DatabaseImageDAO implements ImageDAO {

    private static final String INSERT_SQL =
            "INSERT INTO images (engine_id, filename, uploaded_by) VALUES (?, ?, ?)";

    private static final String DELETE_SQL =
            "DELETE FROM images WHERE id = ?";

    private static final String FIND_BY_ID_SQL =
            "SELECT * FROM images WHERE id = ?";

    private static final String FIND_BY_ENGINE_SQL =
            "SELECT * FROM images WHERE engine_id = ? ORDER BY upload_date DESC";

    // "prima immagine" = la più vecchia (ASC). Se vuoi la più recente, metti DESC.
    private static final String FIND_COVER_BY_ENGINE_SQL =
            "SELECT * FROM images WHERE engine_id = ? ORDER BY upload_date ASC LIMIT 1";

    @Override
    public Image save(Image image) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, image.getEngineId());
            stmt.setString(2, image.getFilename());

            if (image.getUploadedBy() != null) {
                stmt.setLong(3, image.getUploadedBy());
            } else {
                stmt.setNull(3, Types.BIGINT);
            }

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long newId = rs.getLong(1);
                    // Ricarico il record per avere anche upload_date generato dal DB
                    return findById(newId).orElseThrow(() ->
                            new RuntimeException("Impossibile ricaricare l'immagine appena salvata: id=" + newId)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio dell'immagine", e);
        }

        throw new RuntimeException("Errore durante il salvataggio dell'immagine");
    }

    @Override
    public boolean delete(long imageId) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setLong(1, imageId);
            int affected = stmt.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione dell'immagine", e);
        }
    }

    @Override
    public Optional<Image> findById(long imageId) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {

            stmt.setLong(1, imageId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero dell'immagine", e);
        }

        return Optional.empty();
    }

    @Override
    public List<Image> findAllByEngineId(long engineId) {
        List<Image> images = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ENGINE_SQL)) {

            stmt.setLong(1, engineId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    images.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero delle immagini del motore", e);
        }

        return images;
    }

    @Override
    public Optional<Image> findCoverByEngineId(long engineId) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_COVER_BY_ENGINE_SQL)) {

            stmt.setLong(1, engineId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero della cover del motore", e);
        }

        return Optional.empty();
    }

    /* =========================
       Mapping
       ========================= */

    private Image mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("upload_date");
        return new Image(
                rs.getLong("id"),
                rs.getLong("engine_id"),
                rs.getString("filename"),
                rs.getObject("uploaded_by", Long.class),
                ts != null ? ts.toLocalDateTime() : null
        );
    }
}