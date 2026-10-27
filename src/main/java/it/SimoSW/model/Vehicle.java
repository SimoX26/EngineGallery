package it.SimoSW.model;

public class Vehicle {
    private final long id;
    private final String plate, make, model, notes;

    public Vehicle(long id, String plate, String make, String model, String notes) {
        this.id = id;
        this.plate = plate;
        this.make = make;
        this.model = model;
        this.notes = notes;
    }

    public long getId() { return id; }
    public String getPlate() { return plate; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public String getNotes() { return notes; }
}
