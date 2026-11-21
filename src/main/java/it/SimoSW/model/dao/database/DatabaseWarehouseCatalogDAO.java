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

    public void addCategory(String department, String name, boolean injectorFields) {
        execute("INSERT INTO warehouse_categories (department, name, injector_fields) VALUES (?, ?, ?)", department, name, injectorFields);
    }

    public void renameCategory(Long id, String name) {
        execute("UPDATE warehouse_categories SET name = ? WHERE id = ?", name, id);
    }

    public void deleteCategory(Long id) {
        // Preserve classification of existing items.
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM warehouse_item_details WHERE category_id = ?")) {
            check.setLong(1, id);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) throw new IllegalArgumentException("Categoria in uso: sposta prima gli articoli");
            }
        } catch (SQLException e) { throw new RuntimeException("Errore nel controllo della categoria", e); }
        execute("DELETE FROM warehouse_categories WHERE id = ?", id);
    }

    private void execute(String sql, Object... values) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) stmt.setObject(i + 1, values[i]);
            stmt.executeUpdate();
        } catch (SQLException e) { throw new IllegalArgumentException("Valore già presente o non valido", e); }
    }
}
