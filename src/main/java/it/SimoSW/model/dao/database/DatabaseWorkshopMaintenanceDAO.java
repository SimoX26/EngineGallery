package it.SimoSW.model.dao.database;

import it.SimoSW.model.MaintenanceIntervention;
import it.SimoSW.model.MaintenancePlan;
import it.SimoSW.model.WorkshopMachine;
import it.SimoSW.model.dao.WorkshopMaintenanceDAO;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseWorkshopMaintenanceDAO implements WorkshopMaintenanceDAO {
    @Override
    public List<WorkshopMachine> findMachines() {
        List<WorkshopMachine> result = new ArrayList<>();
        String sql = "SELECT id, name, model, serial_number, location, status, usage_hours FROM workshop_machines ORDER BY name, id";
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                result.add(new WorkshopMachine(rows.getLong("id"), rows.getString("name"), rows.getString("model"),
                        rows.getString("serial_number"), rows.getString("location"), rows.getString("status"), rows.getInt("usage_hours")));
            }
        } catch (SQLException e) { throw new IllegalStateException("Errore nel caricamento dei macchinari", e); }
        return result;
    }

    @Override
    public List<MaintenancePlan> findPlans() {
        List<MaintenancePlan> result = new ArrayList<>();
        String sql = "SELECT p.id, p.machine_id, m.name AS machine_name, p.description, p.due_date, p.due_hours, p.completed, m.usage_hours "
                + "FROM workshop_maintenance_plans p JOIN workshop_machines m ON m.id = p.machine_id "
                + "ORDER BY p.completed, p.due_date, p.due_hours, p.id";
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                Date date = rows.getDate("due_date");
                int hours = rows.getInt("due_hours");
                Integer dueHours = rows.wasNull() ? null : hours;
                result.add(new MaintenancePlan(rows.getLong("id"), rows.getLong("machine_id"), rows.getString("machine_name"),
                        rows.getString("description"), date == null ? null : date.toLocalDate(), dueHours,
                        rows.getBoolean("completed"), rows.getInt("usage_hours")));
            }
        } catch (SQLException e) { throw new IllegalStateException("Errore nel caricamento dei piani", e); }
        return result;
    }

    @Override
    public List<MaintenanceIntervention> findInterventions() {
        List<MaintenanceIntervention> result = new ArrayList<>();
        String sql = "SELECT i.id, i.machine_id, m.name AS machine_name, i.type, i.performed_on, i.description, i.parts, i.cost, i.operator_name "
                + "FROM workshop_maintenance_interventions i JOIN workshop_machines m ON m.id = i.machine_id "
                + "ORDER BY i.performed_on DESC, i.id DESC";
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                result.add(new MaintenanceIntervention(rows.getLong("id"), rows.getLong("machine_id"), rows.getString("machine_name"),
                        rows.getString("type"), rows.getDate("performed_on").toLocalDate(), rows.getString("description"),
                        rows.getString("parts"), rows.getBigDecimal("cost"), rows.getString("operator_name")));
            }
        } catch (SQLException e) { throw new IllegalStateException("Errore nel caricamento degli interventi", e); }
        return result;
    }

    @Override
    public long createMachine(String name, String model, String serialNumber, String location, int usageHours) {
        String sql = "INSERT INTO workshop_machines (name, model, serial_number, location, usage_hours) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name); statement.setString(2, model); statement.setString(3, serialNumber);
            statement.setString(4, location); statement.setInt(5, usageHours);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) { if (keys.next()) return keys.getLong(1); }
            throw new IllegalStateException("ID macchinario non disponibile");
        } catch (SQLException e) { throw new IllegalStateException("Errore nel salvataggio del macchinario (matricola già presente?)", e); }
    }

    @Override
    public boolean updateMachine(long id, String name, String model, String serialNumber, String location, String status, int usageHours) {
        String sql = "UPDATE workshop_machines SET name=?, model=?, serial_number=?, location=?, status=?, usage_hours=? WHERE id=?";
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name); statement.setString(2, model); statement.setString(3, serialNumber);
            statement.setString(4, location); statement.setString(5, status); statement.setInt(6, usageHours); statement.setLong(7, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) { throw new IllegalStateException("Errore nell'aggiornamento del macchinario (matricola già presente?)", e); }
    }

    @Override
    public long createPlan(long machineId, String description, LocalDate dueDate, Integer dueHours) {
        String sql = "INSERT INTO workshop_maintenance_plans (machine_id, description, due_date, due_hours) VALUES (?, ?, ?, ?)";
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, machineId); statement.setString(2, description);
            if (dueDate == null) statement.setNull(3, java.sql.Types.DATE); else statement.setDate(3, Date.valueOf(dueDate));
            if (dueHours == null) statement.setNull(4, java.sql.Types.INTEGER); else statement.setInt(4, dueHours);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) { if (keys.next()) return keys.getLong(1); }
            throw new IllegalStateException("ID piano non disponibile");
        } catch (SQLException e) { throw new IllegalStateException("Errore nel salvataggio del piano", e); }
    }

    @Override
    public void recordIntervention(long machineId, Long planId, String type, LocalDate performedOn,
                                   String description, String parts, BigDecimal cost, String operator) {
        String insert = "INSERT INTO workshop_maintenance_interventions (machine_id, plan_id, type, performed_on, description, parts, cost, operator_name) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = ConnectionFactory.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (planId != null) {
                    try (PreparedStatement lock = connection.prepareStatement(
                            "SELECT id FROM workshop_maintenance_plans WHERE id=? AND machine_id=? AND completed=0 FOR UPDATE")) {
                        lock.setLong(1, planId); lock.setLong(2, machineId);
                        try (ResultSet rows = lock.executeQuery()) {
                            if (!rows.next()) throw new IllegalArgumentException("Piano non disponibile per questo macchinario");
                        }
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(insert)) {
                    statement.setLong(1, machineId);
                    if (planId == null) statement.setNull(2, java.sql.Types.BIGINT); else statement.setLong(2, planId);
                    statement.setString(3, type); statement.setDate(4, Date.valueOf(performedOn));
                    statement.setString(5, description); statement.setString(6, parts);
                    statement.setBigDecimal(7, cost); statement.setString(8, operator);
                    statement.executeUpdate();
                }
                if (planId != null) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE workshop_maintenance_plans SET completed=1 WHERE id=?")) {
                        statement.setLong(1, planId); statement.executeUpdate();
                    }
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback(); throw e;
            }
        } catch (SQLException e) { throw new IllegalStateException("Errore nel salvataggio dell'intervento", e); }
    }
}
