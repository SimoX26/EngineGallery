package it.SimoSW.model;

public class WarehouseCompatibility {
    private final String engineCode;
    private final boolean verified;

    public WarehouseCompatibility(String engineCode, boolean verified) {
        this.engineCode = engineCode;
        this.verified = verified;
    }

    public String getEngineCode() { return engineCode; }
    public boolean isVerified() { return verified; }
}
