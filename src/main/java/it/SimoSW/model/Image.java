package it.SimoSW.model;

import java.time.Instant;

public class Image {

    private long id;
    private String filePath;
    private long folderId;
    private Instant uploadDate;

    public Image() {
        // costruttore vuoto richiesto per mapping DAO
    }

    public Image(String filePath, long folderId) {
        this.filePath = filePath;
        this.folderId = folderId;
    }

    // =========================
    // Getter & Setter
    // =========================

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFolderId() {
        return folderId;
    }

    public void setFolderId(long folderId) {
        this.folderId = folderId;
    }

    public Instant getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Instant uploadDate) {
        this.uploadDate = uploadDate;
    }
}
