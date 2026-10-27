package it.SimoSW.controller.app;

import it.SimoSW.model.Vehicle;
import it.SimoSW.model.VehicleEvent;
import it.SimoSW.model.VehicleReminder;
import it.SimoSW.model.dao.VehicleDAO;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class VehicleController {
    private final VehicleDAO dao;

    public VehicleController(VehicleDAO dao) { this.dao = dao; }

    public List<Vehicle> getVehicles() { return dao.findAll(); }
    public Optional<Vehicle> getVehicle(long id) { return dao.findById(id); }
    public List<VehicleEvent> getEvents(long vehicleId) { return dao.findEvents(vehicleId); }
    public List<VehicleReminder> getPendingEvents() { return dao.findPendingEvents(); }
    public Optional<VehicleEvent> getEvent(long vehicleId, long eventId) { return dao.findEvent(vehicleId, eventId); }

    public long createVehicle(String plate, String make, String model, String notes) {
        return dao.create(required(plate, 20, "Targa").toUpperCase(java.util.Locale.ROOT),
                required(make, 100, "Marca"), required(model, 100, "Modello"), optional(notes, 2000, "Note"));
    }

    public void updateVehicle(long id, String plate, String make, String model, String notes) {
        if (id <= 0 || !dao.update(id, required(plate, 20, "Targa").toUpperCase(java.util.Locale.ROOT),
                required(make, 100, "Marca"), required(model, 100, "Modello"), optional(notes, 2000, "Note")))
            throw new IllegalArgumentException("Veicolo non trovato");
    }

    public void deleteVehicle(long id) {
        if (id <= 0 || !dao.delete(id)) throw new IllegalArgumentException("Veicolo non trovato");
    }

    public void setPhotoFilenames(long id, String vehiclePhotoFilename, String registrationPhotoFilename) {
        dao.setPhotoFilenames(id, vehiclePhotoFilename, registrationPhotoFilename);
    }

    public void createEvent(long vehicleId, String type, String title, LocalDate dueDate, LocalDate completedOn, String notes) {
        if (vehicleId <= 0 || dao.findById(vehicleId).isEmpty()) throw new IllegalArgumentException("Veicolo non trovato");
        validateEvent(type, title, dueDate, completedOn, notes);
        dao.createEvent(vehicleId, type, title.trim(), dueDate, completedOn, optional(notes, 2000, "Note"));
    }

    public void updateEvent(long vehicleId, long eventId, String type, String title, LocalDate dueDate,
                            LocalDate completedOn, String notes) {
        validateEvent(type, title, dueDate, completedOn, notes);
        if (vehicleId <= 0 || eventId <= 0 || !dao.updateEvent(vehicleId, eventId, type, title.trim(), dueDate,
                completedOn, optional(notes, 2000, "Note"))) throw new IllegalArgumentException("Voce non trovata");
    }

    public void deleteEvent(long vehicleId, long eventId) {
        if (vehicleId <= 0 || eventId <= 0 || !dao.deleteEvent(vehicleId, eventId))
            throw new IllegalArgumentException("Voce non trovata");
    }

    private static void validateEvent(String type, String title, LocalDate dueDate, LocalDate completedOn, String notes) {
        if (!"DEADLINE".equals(type) && !"MAINTENANCE".equals(type))
            throw new IllegalArgumentException("Tipo di voce non valido");
        required(title, 150, "Titolo");
        if (dueDate == null) throw new IllegalArgumentException("Indica la data di scadenza o dell'intervento");
        if (completedOn != null && completedOn.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("La data di completamento non può essere futura");
        optional(notes, 2000, "Note");
    }

    private static String required(String value, int max, String label) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty() || trimmed.length() > max)
            throw new IllegalArgumentException(label + " obbligatorio (massimo " + max + " caratteri)");
        return trimmed;
    }

    private static String optional(String value, int max, String label) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.length() > max) throw new IllegalArgumentException(label + ": massimo " + max + " caratteri");
        return trimmed.isEmpty() ? null : trimmed;
    }
}
