package it.SimoSW.model.dao.database;

import it.SimoSW.model.Image;
import it.SimoSW.model.dao.ImageDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Implementazione JDBC di ImageDAO.
 */
public class DatabaseImageDAO implements ImageDAO {

    private static final String INSERT_SQL =
            "INSERT INTO image (engine_id, file_path, description, keywords) VALUES (?, ?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE image SET file_path = ?, description = ?, keywords = ? WHERE id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM image WHERE id = ?";

    private static final String FIND_BY_ID_SQL =
            "SELECT * FROM image WHERE id = ?";

    private static final String FIND_BY_ENGINE_SQL =
            "SELECT * FROM image WHERE engine_id = ?";

    @Override
    public Image save(Image image) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, image.getEngineId());
            stmt.setString(2, image.getFilePath());
            stmt.setString(3, image.getDescription());
            stmt.setString(4, String.join(",", image.getKeywords()));

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Image(
                            rs.getLong(1),
                            image.getEngineId(),
                            image.getFilePath(),
                            image.getDescription(),
                            image.getKeywords()
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio dell'immagine", e);
        }

        throw new RuntimeException("Errore durante il salvataggio dell'immagine");
    }

    @Override
    public Image update(Image image) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setString(1, image.getFilePath());
            stmt.setString(2, image.getDescription());
            stmt.setString(3, String.join(",", image.getKeywords()));
            stmt.setLong(4, image.getId());

            stmt.executeUpdate();
            return image;

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento dell'immagine", e);
        }
    }

    @Override
    public void delete(long imageId) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setLong(1, imageId);
            stmt.executeUpdate();

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
    public List<Image> findByEngineId(long engineId) {
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

    /* =========================
       Mapping
       ========================= */

    private Image mapRow(ResultSet rs) throws SQLException {
        List<String> keywords = new ArrayList<>();
        String rawKeywords = rs.getString("keywords");

        if (rawKeywords != null && !rawKeywords.isEmpty()) {
            keywords = Arrays.asList(rawKeywords.split(","));
        }

        return new Image(
                rs.getLong("id"),
                rs.getLong("engine_id"),
                rs.getString("file_path"),
                rs.getString("description"),
                keywords
        );
    }
}
