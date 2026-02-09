package it.SimoSW.util.bean;


public class EngineBean {

    private String engineRef;
    private String engineCode;
    private long customerId;
    private String status;
    private String intakeDate;
    private String deliveryDate;
    private String notes;

    public EngineBean() {}

    /* =========================
       Getter
       ========================= */

    public String getEngineRef() {
        return engineRef;
    }

    public String getEngineCode() {
        return engineCode;
    }

    public long getCustomerId() {
        return customerId;
    }

    public String getStatus() {
        return status;
    }

    public String getIntakeDate() {
        return intakeDate;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public String getNotes() {
        return notes;
    }

    /* =========================
       Setter
       ========================= */

    public void setEngineRef(String engineRef) {
        this.engineRef = engineRef;
    }

    public void setEngineCode(String engineCode) {
        this.engineCode = engineCode;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setIntakeDate(String intakeDate) {
        this.intakeDate = intakeDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}