package it.SimoSW.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class WarehouseImage {

    private long id;
    private final long warehouseItemId;
    private final String filename;
    private final Long uploadedBy;
    private LocalDateTime uploadDate;

    public WarehouseImage(long warehouseItemId, String filename, Long uploadedBy) {
        this.warehouseItemId = warehouseItemId;
        this.filename = Objects.requireNonNull(filename);
        this.uploadedBy = uploadedBy;
        this.uploadDate = LocalDateTime.now();
    }

    public WarehouseImage(long id, long warehouseItemId, String filename, Long uploadedBy, LocalDateTime uploadDate) {
        this(warehouseItemId, filename, uploadedBy);
        this.id = id;
        this.uploadDate = uploadDate;
    }

    public long getId() {
        return id;
    }

    public long getWarehouseItemId() {
        return warehouseItemId;
    }

    public String getFilename() {
        return filename;
    }

    public Long getUploadedBy() {
        return uploadedBy;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }
}
