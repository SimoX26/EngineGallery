package it.SimoSW.model;

public class Metadata {

    private long imageId;
    private String clientName;
    private String engineCode;
    private EngineStatus engineStatus;
    private String description;

    public Metadata() {
        // costruttore vuoto richiesto per mapping DAO
    }

    public Metadata(long imageId,
                    String clientName,
                    String engineCode,
                    EngineStatus engineStatus,
                    String description) {
        this.imageId = imageId;
        this.clientName = clientName;
        this.engineCode = engineCode;
        this.engineStatus = engineStatus;
        this.description = description;
    }

    // =========================
    // Getter & Setter
    // =========================

    public long getImageId() {
        return imageId;
    }

    public void setImageId(long imageId) {
        this.imageId = imageId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getEngineCode() {
        return engineCode;
    }

    public void setEngineCode(String engineCode) {
        this.engineCode = engineCode;
    }

    public EngineStatus getEngineStatus() {
        return engineStatus;
    }

    public void setEngineStatus(EngineStatus engineStatus) {
        this.engineStatus = engineStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
