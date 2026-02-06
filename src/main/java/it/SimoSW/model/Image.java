package it.SimoSW.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Image {

    /* =========================
       Identità tecnica (DB)
       ========================= */
    private long id;

    /* =========================
       Relazione
       ========================= */
    private final long engineId;

    /* =========================
       File
       ========================= */
    private final String filename;

    /* =========================
       Audit
       ========================= */
    private final Long uploadedBy;
    private LocalDateTime uploadDate;

    /* =========================
       Costruttore per nuova Image
       ========================= */
    public Image(long engineId, String filename, Long uploadedBy) {
        this.engineId = engineId;
        this.filename = Objects.requireNonNull(filename);
        this.uploadedBy = uploadedBy;
        this.uploadDate = LocalDateTime.now();
    }

    /* =========================
       Costruttore per Image persistita
       ========================= */
    public Image(long id, long engineId, String filename, Long uploadedBy, LocalDateTime uploadDate) {
        this(engineId, filename, uploadedBy);
        this.id = id;
        this.uploadDate = uploadDate;
    }

    /* =========================
       Getter
       ========================= */
    public long getId() {
        return id;
    }

    public long getEngineId() {
        return engineId;
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