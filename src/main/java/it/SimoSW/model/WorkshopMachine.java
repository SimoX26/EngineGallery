package it.SimoSW.model;

import java.time.LocalDate;

public class WorkshopMachine {
    private final long id;
    private final String name, model, serialNumber, location, status, imageFilename;
    private final int usageHours;
    private final LocalDate purchaseDate;

    public WorkshopMachine(long id, String name, String model, String serialNumber, String location, LocalDate purchaseDate,
                           String status, int usageHours, String imageFilename) {
        this.id = id;
        this.name = name;
        this.model = model;
        this.serialNumber = serialNumber;
        this.location = location;
        this.purchaseDate = purchaseDate;
        this.status = status;
        this.usageHours = usageHours;
        this.imageFilename = imageFilename;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getModel() { return model; }
    public String getSerialNumber() { return serialNumber; }
    public String getLocation() { return location; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public String getStatus() { return status; }
    public int getUsageHours() { return usageHours; }
    public String getImageFilename() { return imageFilename; }
}
