package it.SimoSW.model.dao.database;

import it.SimoSW.model.CatalogItem;
import it.SimoSW.model.dao.CatalogItemDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseCatalogItemDAO implements CatalogItemDAO {

    private static final String FIND_ALL_SQL = """
        SELECT id, cylinder_diameter_mm, engine_model, displacement_cc, valve_count, engine_code
        FROM catalog_items
        ORDER BY engine_model, engine_code, id
    """;

    private static final String FIND_BY_ID_SQL = """
        SELECT id, cylinder_diameter_mm, engine_model, displacement_cc, valve_count, engine_code
        FROM catalog_items
        WHERE id = ?
    """;

    @Override
    public List<CatalogItem> findAll() {
        List<CatalogItem> items = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                items.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero delle voci di catalogo", e);
        }

        return items;
    }

    @Override
    public Optional<CatalogItem> findById(long id) {
        try (Connection conn = ConnectionFactory.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID_SQL)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero della voce di catalogo ID: " + id, e);
        }

        return Optional.empty();
    }

    private CatalogItem mapRow(ResultSet rs) throws SQLException {
        return new CatalogItem(
                rs.getLong("id"),
                rs.getBigDecimal("cylinder_diameter_mm"),
                rs.getString("engine_model"),
                rs.getInt("displacement_cc"),
                rs.getInt("valve_count"),
                rs.getString("engine_code")
        );
    }
}
