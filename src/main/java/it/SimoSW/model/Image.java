package it.SimoSW.model;

import java.time.LocalDateTime;

public class Image {

    private long id;
    private long engineId;
    private String filename;
    private LocalDateTime uploadDate;
    private Long uploadedBy;

    // Costruttore completo (tipicamente usato dal DAO in lettura)
    public Image(long id, long engineId, String filename, LocalDateTime uploadDate, Long uploadedBy) {
        this.id = id;
        this.engineId = engineId;
        this.filename = filename;
        this.uploadDate = uploadDate;
        this.uploadedBy = uploadedBy;
    }

    // Costruttore per creazione nuova immagine (prima della persistenza)
    public Image(long engineId, String filename) {
        this.engineId = engineId;
        this.filename = filename;
    }

    public long getId() {
        return id;
    }

    public long getEngineId() {
        return engineId;
    }

    public String getFilename() {
        return filename;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public Long getUploadedBy() {
        return uploadedBy;
    }
}
