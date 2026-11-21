package it.SimoSW.model.dao.database;

import it.SimoSW.model.WarehouseCompatibility;
import it.SimoSW.model.WarehouseDetails;
import it.SimoSW.model.WarehouseItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class WarehouseItemDetailsDAO {
    private WarehouseItemDetailsDAO() { }

    static void attach(Connection conn, List<WarehouseItem> items) throws SQLException {
        if (items.isEmpty()) return;
        Map<Long, WarehouseItem> byId = new HashMap<>();
        for (WarehouseItem item : items) byId.put(item.getId(), item);
        String placeholders = items.stream().map(item -> "?").collect(Collectors.joining(","));
        Map<Long, List<WarehouseCompatibility>> compatibility = new HashMap<>();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT item_id, engine_code, verified FROM warehouse_engine_compatibilities WHERE item_id IN (" + placeholders + ") ORDER BY engine_code")) {
            bindIds(stmt, items);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) compatibility.computeIfAbsent(rs.getLong("item_id"), key -> new ArrayList<>())
                        .add(new WarehouseCompatibility(rs.getString("engine_code"), rs.getBoolean("verified")));
            }
        }
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT d.*, c.department, c.name AS category_name, c.injector_fields FROM warehouse_item_details d " +
                "LEFT JOIN warehouse_categories c ON c.id = d.category_id WHERE d.item_id IN (" + placeholders + ")")) {
            bindIds(stmt, items);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long itemId = rs.getLong("item_id");
                    Long categoryId = rs.getObject("category_id", Long.class);
                    WarehouseDetails details = new WarehouseDetails(categoryId, rs.getString("manufacturer"),
                            rs.getString("oe_code"), rs.getString("alternative_codes"), rs.getString("condition_label"),
                            rs.getString("lot_code"), rs.getString("source_engine_code"), rs.getString("technical_notes"),
                            rs.getString("manufacturer_code"), rs.getString("calibration_code"), rs.getString("source_cylinder"),
                            rs.getString("test_status"), rs.getString("revision_status"),
                            compatibility.getOrDefault(itemId, List.of()));
                    details.setCategoryLabels(rs.getString("department"), rs.getString("category_name"), rs.getBoolean("injector_fields"));
                    byId.get(itemId).setDetails(details);
                }
            }
        }
    }

    private static void bindIds(PreparedStatement stmt, List<WarehouseItem> items) throws SQLException {
        for (int i = 0; i < items.size(); i++) stmt.setLong(i + 1, items.get(i).getId());
    }

    static void write(Connection conn, WarehouseItem item) throws SQLException {
        WarehouseDetails d = item.getDetails();
        if (d == null) return;
        String sql = "INSERT INTO warehouse_item_details (item_id, category_id, manufacturer, oe_code, alternative_codes, " +
                "condition_label, lot_code, source_engine_code, technical_notes, manufacturer_code, calibration_code, " +
                "source_cylinder, test_status, revision_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE category_id=VALUES(category_id), manufacturer=VALUES(manufacturer), " +
                "oe_code=VALUES(oe_code), alternative_codes=VALUES(alternative_codes), condition_label=VALUES(condition_label), " +
                "lot_code=VALUES(lot_code), source_engine_code=VALUES(source_engine_code), technical_notes=VALUES(technical_notes), " +
                "manufacturer_code=VALUES(manufacturer_code), calibration_code=VALUES(calibration_code), " +
                "source_cylinder=VALUES(source_cylinder), test_status=VALUES(test_status), revision_status=VALUES(revision_status)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, item.getId());
            if (d.getCategoryId() == null) stmt.setNull(2, Types.BIGINT);
            else stmt.setLong(2, d.getCategoryId());
            stmt.setString(3, d.getManufacturer());
            stmt.setString(4, d.getOeCode());
            stmt.setString(5, d.getAlternativeCodes());
            stmt.setString(6, d.getCondition());
            stmt.setString(7, d.getLotCode());
            stmt.setString(8, d.getSourceEngineCode());
            stmt.setString(9, d.getTechnicalNotes());
            stmt.setString(10, d.getManufacturerCode());
            stmt.setString(11, d.getCalibrationCode());
            stmt.setString(12, d.getSourceCylinder());
            stmt.setString(13, d.getTestStatus());
            stmt.setString(14, d.getRevisionStatus());
            stmt.executeUpdate();
        }
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM warehouse_engine_compatibilities WHERE item_id = ?")) {
            stmt.setLong(1, item.getId());
            stmt.executeUpdate();
        }
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO warehouse_engine_compatibilities (item_id, engine_code, verified) VALUES (?, ?, ?)")) {
            for (WarehouseCompatibility compatibility : d.getCompatibilities()) {
                stmt.setLong(1, item.getId());
                stmt.setString(2, compatibility.getEngineCode());
                stmt.setBoolean(3, compatibility.isVerified());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
}
