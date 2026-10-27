package it.SimoSW.model;

import java.time.LocalDate;

public class VehicleEvent {
    private final long id, vehicleId;
    private final String type, title, notes;
    private final LocalDate dueDate, completedOn;

    public VehicleEvent(long id, long vehicleId, String type, String title, LocalDate dueDate,
                        LocalDate completedOn, String notes) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.type = type;
        this.title = title;
        this.dueDate = dueDate;
        this.completedOn = completedOn;
        this.notes = notes;
    }

    public long getId() { return id; }
    public long getVehicleId() { return vehicleId; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getCompletedOn() { return completedOn; }
    public String getNotes() { return notes; }
    public boolean isOverdue() { return completedOn == null && dueDate.isBefore(LocalDate.now()); }
    public boolean isDueSoon() { return completedOn == null && !isOverdue() && !dueDate.isAfter(LocalDate.now().plusDays(30)); }
}
