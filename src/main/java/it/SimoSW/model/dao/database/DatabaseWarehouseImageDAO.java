package it.SimoSW.model.dao.database;

import it.SimoSW.model.WarehouseImage;
import it.SimoSW.model.dao.WarehouseImageDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseWarehouseImageDAO implements WarehouseImageDAO {

    private static final String INSERT_SQL =
            "INSERT INTO warehouse_images (warehouse_item_id, filename, uploaded_by) VALUES (?, ?, ?)";

    private static final String DELETE_SQL =
            "DELETE FROM warehouse_images WHERE id = ?";

    private static final String FIND_BY_ID_SQL =
            "SELECT * FROM warehouse_images WHERE id = ?";

    private static final String FIND_BY_ITEM_SQL =
            "SELECT * FROM warehouse_images WHERE warehouse_item_id = ? ORDER BY upload_date DESC";

    @Override
    public WarehouseImage save(WarehouseImage image) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, image.getWarehouseItemId());
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
                    return findById(newId).orElseThrow(() ->
                            new RuntimeException("Impossibile ricaricare l'immagine magazzino appena salvata: id=" + newId)
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio dell'immagine articolo", e);
        }

        throw new RuntimeException("Errore durante il salvataggio dell'immagine articolo");
    }

    @Override
    public boolean delete(long imageId) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setLong(1, imageId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione dell'immagine articolo", e);
        }
    }

    @Override
    public Optional<WarehouseImage> findById(long imageId) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {

            stmt.setLong(1, imageId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero dell'immagine articolo", e);
        }

        return Optional.empty();
    }

    @Override
    public List<WarehouseImage> findAllByWarehouseItemId(long itemId) {
        List<WarehouseImage> images = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ITEM_SQL)) {

            stmt.setLong(1, itemId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    images.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero delle immagini articolo", e);
        }

        return images;
    }

    private WarehouseImage mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("upload_date");
        return new WarehouseImage(
                rs.getLong("id"),
                rs.getLong("warehouse_item_id"),
                rs.getString("filename"),
                rs.getObject("uploaded_by", Long.class),
                ts != null ? ts.toLocalDateTime() : null
        );
    }
}
