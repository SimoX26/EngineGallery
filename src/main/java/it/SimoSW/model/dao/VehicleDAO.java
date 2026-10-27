package it.SimoSW.model.dao;

import it.SimoSW.model.Vehicle;
import it.SimoSW.model.VehicleEvent;
import it.SimoSW.model.VehicleReminder;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VehicleDAO {
    List<Vehicle> findAll();
    Optional<Vehicle> findById(long id);
    long create(String plate, String make, String model, String notes);
    boolean update(long id, String plate, String make, String model, String notes);
    void setPhotoFilenames(long id, String vehiclePhotoFilename, String registrationPhotoFilename);
    boolean delete(long id);
    List<VehicleEvent> findEvents(long vehicleId);
    List<VehicleReminder> findPendingEvents();
    Optional<VehicleEvent> findEvent(long vehicleId, long eventId);
    void createEvent(long vehicleId, String type, String title, LocalDate dueDate, LocalDate completedOn, String notes);
    boolean updateEvent(long vehicleId, long eventId, String type, String title, LocalDate dueDate, LocalDate completedOn, String notes);
    boolean deleteEvent(long vehicleId, long eventId);
}
