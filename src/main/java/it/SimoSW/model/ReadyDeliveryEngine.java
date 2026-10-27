package it.SimoSW.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReadyDeliveryEngine {
    private static final DateTimeFormatter CREATED_AT_FORMATTER =
            DateTimeFormatter.ofPattern("dd / MM / yyyy · HH:mm");

    private final long id;
    private final String engineCode;
    private final String model;
    private final int quantity;
    private final String notes;
    private final String imageFilename;
    private final LocalDateTime createdAt;
    private final String createdBy;

    public ReadyDeliveryEngine(long id, String engineCode, String model, int quantity, String notes,
                               String imageFilename, LocalDateTime createdAt, String createdBy) {
        this.id = id;
        this.engineCode = engineCode;
        this.model = model;
        this.quantity = quantity;
        this.notes = notes;
        this.imageFilename = imageFilename;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    public long getId() { return id; }
    public String getEngineCode() { return engineCode; }
    public String getModel() { return model; }
    public int getQuantity() { return quantity; }
    public String getNotes() { return notes; }
    public String getImageFilename() { return imageFilename; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }
    public String getCreatedAtLabel() { return createdAt == null ? "" : CREATED_AT_FORMATTER.format(createdAt); }
}
