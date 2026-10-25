package it.SimoSW.model;

public class WorkshopMachine {
    private final long id;
    private final String name, model, serialNumber, location, status, imageFilename;
    private final int usageHours;

    public WorkshopMachine(long id, String name, String model, String serialNumber, String location, String status, int usageHours, String imageFilename) {
        this.id = id;
        this.name = name;
        this.model = model;
        this.serialNumber = serialNumber;
        this.location = location;
        this.status = status;
        this.usageHours = usageHours;
        this.imageFilename = imageFilename;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getModel() { return model; }
    public String getSerialNumber() { return serialNumber; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }
    public int getUsageHours() { return usageHours; }
    public String getImageFilename() { return imageFilename; }
}
