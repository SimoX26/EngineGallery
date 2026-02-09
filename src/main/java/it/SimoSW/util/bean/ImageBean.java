package it.SimoSW.util.bean;

public class ImageBean {

    /* =========================
       File
       ========================= */
    private String filename;

    /* =========================
       Audit (UI-friendly)
       ========================= */
    private String uploadDate;   // ISO / formattata

    public ImageBean() {}

    /* =========================
       Getter
       ========================= */

    public String getFilename() {
        return filename;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    /* =========================
       Setter
       ========================= */

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }
}