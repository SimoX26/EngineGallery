package it.SimoSW.model.dao.database;

import it.SimoSW.model.WarehouseItem;
import it.SimoSW.model.dao.WarehouseItemDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseWarehouseItemDAO implements WarehouseItemDAO {

    private static final String FIND_ALL_SQL =
            "SELECT id, name, sku, quantity, location, notes FROM warehouse_items ORDER BY name";

    private static final String FIND_BY_ID_SQL =
            "SELECT id, name, sku, quantity, location, notes FROM warehouse_items WHERE id = ?";

    private static final String INSERT_SQL =
            "INSERT INTO warehouse_items (name, sku, quantity, location, notes) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE warehouse_items SET name = ?, sku = ?, quantity = ?, location = ?, notes = ? WHERE id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM warehouse_items WHERE id = ?";

    @Override
    public List<WarehouseItem> findAll() {

        List<WarehouseItem> items = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                items.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero articoli magazzino", e);
        }

        return items;
    }

    @Override
    public Optional<WarehouseItem> findById(Long id) {

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero articolo magazzino ID: " + id, e);
        }

        return Optional.empty();
    }

    @Override
    public Long save(WarehouseItem item) {

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, item.getName());
            stmt.setString(2, item.getSku());
            stmt.setInt(3, item.getQuantity());
            stmt.setString(4, item.getLocation());
            stmt.setString(5, item.getNotes());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio articolo magazzino", e);
        }

        throw new RuntimeException("Impossibile ottenere ID generato per articolo magazzino");
    }

    @Override
    public void update(WarehouseItem item) {

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {

            stmt.setString(1, item.getName());
            stmt.setString(2, item.getSku());
            stmt.setInt(3, item.getQuantity());
            stmt.setString(4, item.getLocation());
            stmt.setString(5, item.getNotes());
            stmt.setLong(6, item.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento articolo magazzino ID: " + item.getId(), e);
        }
    }

    @Override
    public void delete(Long id) {

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione articolo magazzino ID: " + id, e);
        }
    }

    private WarehouseItem mapRow(ResultSet rs) throws SQLException {
        return new WarehouseItem(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("sku"),
                rs.getInt("quantity"),
                rs.getString("location"),
                rs.getString("notes")
        );
    }
}
