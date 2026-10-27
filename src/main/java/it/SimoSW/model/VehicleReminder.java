package it.SimoSW.model;

public class VehicleReminder {
    private final String plate;
    private final VehicleEvent event;

    public VehicleReminder(String plate, VehicleEvent event) {
        this.plate = plate;
        this.event = event;
    }

    public String getPlate() { return plate; }
    public VehicleEvent getEvent() { return event; }
}
