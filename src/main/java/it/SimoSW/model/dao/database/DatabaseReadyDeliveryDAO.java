package it.SimoSW.model.dao.database;

import it.SimoSW.model.ReadyDeliveryEngine;
import it.SimoSW.model.dao.ReadyDeliveryDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseReadyDeliveryDAO implements ReadyDeliveryDAO {
    private static final String COLUMNS = "id, engine_code, model, quantity, notes, image_filename, created_at, created_by";

    @Override
    public List<ReadyDeliveryEngine> findAll() {
        List<ReadyDeliveryEngine> engines = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT " + COLUMNS + " FROM ready_delivery_engines ORDER BY created_at DESC, id DESC");
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) engines.add(mapRow(rows));
            return engines;
        } catch (SQLException ex) {
            throw new IllegalStateException("Errore nel caricamento dei motori pronta consegna", ex);
        }
    }

    @Override
    public Optional<ReadyDeliveryEngine> findById(long id) {
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT " + COLUMNS + " FROM ready_delivery_engines WHERE id=?")) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(mapRow(rows)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Errore nel caricamento del motore pronta consegna", ex);
        }
    }

    @Override
    public long create(String engineCode, String model, int quantity, String notes, String createdBy) {
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO ready_delivery_engines (engine_code, model, quantity, notes, created_by) VALUES (?, ?, ?, ?, ?)",
                     PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, engineCode);
            statement.setString(2, model);
            statement.setInt(3, quantity);
            statement.setString(4, notes);
            statement.setString(5, createdBy);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            throw new IllegalStateException("ID motore non disponibile");
        } catch (SQLException ex) {
            if ("23000".equals(ex.getSQLState()))
                throw new IllegalArgumentException("Codice motore già presente nella pronta consegna", ex);
            throw new IllegalStateException("Errore nel salvataggio del motore pronta consegna", ex);
        }
    }

    @Override
    public void setImageFilename(long id, String filename) {
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE ready_delivery_engines SET image_filename=? WHERE id=?")) {
            statement.setString(1, filename);
            statement.setLong(2, id);
            if (statement.executeUpdate() != 1) throw new IllegalArgumentException("Motore non trovato");
        } catch (SQLException ex) {
            throw new IllegalStateException("Errore nel salvataggio della foto del motore", ex);
        }
    }

    @Override
    public void delete(long id) {
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM ready_delivery_engines WHERE id=?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Errore nell'annullamento del motore pronta consegna", ex);
        }
    }

    private static ReadyDeliveryEngine mapRow(ResultSet rows) throws SQLException {
        Timestamp createdAt = rows.getTimestamp("created_at");
        return new ReadyDeliveryEngine(rows.getLong("id"), rows.getString("engine_code"), rows.getString("model"),
                rows.getInt("quantity"), rows.getString("notes"), rows.getString("image_filename"),
                createdAt == null ? null : createdAt.toLocalDateTime(), rows.getString("created_by"));
    }
}
