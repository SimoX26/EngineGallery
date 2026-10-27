package it.SimoSW.model.dao.database;

import it.SimoSW.model.Vehicle;
import it.SimoSW.model.VehicleEvent;
import it.SimoSW.model.VehicleReminder;
import it.SimoSW.model.dao.VehicleDAO;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseVehicleDAO implements VehicleDAO {
    @Override
    public List<Vehicle> findAll() {
        List<Vehicle> vehicles = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT id, plate, make, model, notes, vehicle_photo_filename, registration_photo_filename FROM vehicles ORDER BY plate");
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) vehicles.add(vehicle(rows));
        } catch (SQLException e) { throw new IllegalStateException("Impossibile caricare i veicoli", e); }
        return vehicles;
    }

    @Override
    public Optional<Vehicle> findById(long id) {
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT id, plate, make, model, notes, vehicle_photo_filename, registration_photo_filename FROM vehicles WHERE id=?")) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(vehicle(rows)) : Optional.empty();
            }
        } catch (SQLException e) { throw new IllegalStateException("Impossibile caricare il veicolo", e); }
    }

    @Override
    public long create(String plate, String make, String model, String notes) {
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO vehicles (plate, make, model, notes) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            setVehicle(statement, plate, make, model, notes);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
            throw new IllegalStateException("Impossibile ottenere l'ID del veicolo");
        } catch (SQLException e) { throw writeError(e); }
    }

    @Override
    public boolean update(long id, String plate, String make, String model, String notes) {
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE vehicles SET plate=?, make=?, model=?, notes=? WHERE id=?")) {
            setVehicle(statement, plate, make, model, notes);
            statement.setLong(5, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) { throw writeError(e); }
    }

    @Override
    public void setPhotoFilenames(long id, String vehiclePhotoFilename, String registrationPhotoFilename) {
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE vehicles SET vehicle_photo_filename=COALESCE(?, vehicle_photo_filename), "
                             + "registration_photo_filename=COALESCE(?, registration_photo_filename) WHERE id=?")) {
            statement.setString(1, vehiclePhotoFilename);
            statement.setString(2, registrationPhotoFilename);
            statement.setLong(3, id);
            if (statement.executeUpdate() == 0) throw new IllegalArgumentException("Veicolo non trovato");
        } catch (SQLException e) { throw new IllegalStateException("Impossibile salvare le foto del veicolo", e); }
    }

    @Override
    public boolean delete(long id) {
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM vehicles WHERE id=?")) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) { throw new IllegalStateException("Impossibile eliminare il veicolo", e); }
    }

    @Override
    public List<VehicleEvent> findEvents(long vehicleId) {
        List<VehicleEvent> events = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, vehicle_id, type, title, due_date, completed_on, notes FROM vehicle_events WHERE vehicle_id=? ORDER BY completed_on IS NOT NULL, due_date, id")) {
            statement.setLong(1, vehicleId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) events.add(event(rows));
            }
        } catch (SQLException e) { throw new IllegalStateException("Impossibile caricare le scadenze", e); }
        return events;
    }

    @Override
    public List<VehicleReminder> findPendingEvents() {
        List<VehicleReminder> reminders = new ArrayList<>();
        String sql = "SELECT e.id, e.vehicle_id, e.type, e.title, e.due_date, e.completed_on, e.notes, v.plate "
                + "FROM vehicle_events e JOIN vehicles v ON v.id=e.vehicle_id "
                + "WHERE e.completed_on IS NULL ORDER BY e.due_date, v.plate, e.id";
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) reminders.add(new VehicleReminder(rows.getString("plate"), event(rows)));
        } catch (SQLException e) { throw new IllegalStateException("Impossibile caricare le scadenze aperte", e); }
        return reminders;
    }

    @Override
    public Optional<VehicleEvent> findEvent(long vehicleId, long eventId) {
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, vehicle_id, type, title, due_date, completed_on, notes FROM vehicle_events WHERE vehicle_id=? AND id=?")) {
            statement.setLong(1, vehicleId);
            statement.setLong(2, eventId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(event(rows)) : Optional.empty();
            }
        } catch (SQLException e) { throw new IllegalStateException("Impossibile caricare la voce", e); }
    }

    @Override
    public void createEvent(long vehicleId, String type, String title, LocalDate dueDate, LocalDate completedOn, String notes) {
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO vehicle_events (vehicle_id, type, title, due_date, completed_on, notes) VALUES (?,?,?,?,?,?)")) {
            statement.setLong(1, vehicleId);
            setEvent(statement, 2, type, title, dueDate, completedOn, notes);
            statement.executeUpdate();
        } catch (SQLException e) { throw new IllegalStateException("Impossibile salvare la voce", e); }
    }

    @Override
    public boolean updateEvent(long vehicleId, long eventId, String type, String title, LocalDate dueDate, LocalDate completedOn, String notes) {
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE vehicle_events SET type=?, title=?, due_date=?, completed_on=?, notes=? WHERE vehicle_id=? AND id=?")) {
            setEvent(statement, 1, type, title, dueDate, completedOn, notes);
            statement.setLong(6, vehicleId);
            statement.setLong(7, eventId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) { throw new IllegalStateException("Impossibile aggiornare la voce", e); }
    }

    @Override
    public boolean deleteEvent(long vehicleId, long eventId) {
        try (Connection connection = ConnectionFactory.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM vehicle_events WHERE vehicle_id=? AND id=?")) {
            statement.setLong(1, vehicleId);
            statement.setLong(2, eventId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) { throw new IllegalStateException("Impossibile eliminare la voce", e); }
    }

    private static void setVehicle(PreparedStatement statement, String plate, String make, String model, String notes) throws SQLException {
        statement.setString(1, plate);
        statement.setString(2, make);
        statement.setString(3, model);
        statement.setString(4, notes);
    }

    private static void setEvent(PreparedStatement statement, int start, String type, String title,
                                 LocalDate dueDate, LocalDate completedOn, String notes) throws SQLException {
        statement.setString(start, type);
        statement.setString(start + 1, title);
        statement.setDate(start + 2, Date.valueOf(dueDate));
        statement.setDate(start + 3, completedOn == null ? null : Date.valueOf(completedOn));
        statement.setString(start + 4, notes);
    }

    private static Vehicle vehicle(ResultSet rows) throws SQLException {
        return new Vehicle(rows.getLong("id"), rows.getString("plate"), rows.getString("make"),
                rows.getString("model"), rows.getString("notes"), rows.getString("vehicle_photo_filename"),
                rows.getString("registration_photo_filename"));
    }

    private static VehicleEvent event(ResultSet rows) throws SQLException {
        Date completed = rows.getDate("completed_on");
        return new VehicleEvent(rows.getLong("id"), rows.getLong("vehicle_id"), rows.getString("type"),
                rows.getString("title"), rows.getDate("due_date").toLocalDate(),
                completed == null ? null : completed.toLocalDate(), rows.getString("notes"));
    }

    private static RuntimeException writeError(SQLException e) {
        if (e.getErrorCode() == 1062) return new IllegalArgumentException("Targa già presente", e);
        return new IllegalStateException("Impossibile salvare il veicolo", e);
    }
}
