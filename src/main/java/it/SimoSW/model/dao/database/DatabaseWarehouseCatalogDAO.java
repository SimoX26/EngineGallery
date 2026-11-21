package it.SimoSW.model.dao.database;

import it.SimoSW.model.WarehouseCategory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseWarehouseCatalogDAO {
    public List<WarehouseCategory> categories() {
        List<WarehouseCategory> result = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, department, name, injector_fields FROM warehouse_categories ORDER BY department, name");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) result.add(new WarehouseCategory(rs.getLong("id"), rs.getString("department"), rs.getString("name"), rs.getBoolean("injector_fields")));
        } catch (SQLException e) { throw new RuntimeException("Errore nel caricamento delle categorie", e); }
        return result;
    }

    public Optional<WarehouseCategory> category(Long id) {
        if (id == null) return Optional.empty();
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, department, name, injector_fields FROM warehouse_categories WHERE id = ?")) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(new WarehouseCategory(rs.getLong("id"), rs.getString("department"), rs.getString("name"), rs.getBoolean("injector_fields"))) : Optional.empty();
            }
        } catch (SQLException e) { throw new RuntimeException("Errore nel caricamento della categoria", e); }
    }

}
