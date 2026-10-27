package it.SimoSW.model;

public class Vehicle {
    private final long id;
    private final String plate, make, model, notes;
    private final String vehiclePhotoFilename, registrationPhotoFilename;

    public Vehicle(long id, String plate, String make, String model, String notes) {
        this(id, plate, make, model, notes, null, null);
    }

    public Vehicle(long id, String plate, String make, String model, String notes,
                   String vehiclePhotoFilename, String registrationPhotoFilename) {
        this.id = id;
        this.plate = plate;
        this.make = make;
        this.model = model;
        this.notes = notes;
        this.vehiclePhotoFilename = vehiclePhotoFilename;
        this.registrationPhotoFilename = registrationPhotoFilename;
    }

    public long getId() { return id; }
    public String getPlate() { return plate; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public String getNotes() { return notes; }
    public String getVehiclePhotoFilename() { return vehiclePhotoFilename; }
    public String getRegistrationPhotoFilename() { return registrationPhotoFilename; }
}
