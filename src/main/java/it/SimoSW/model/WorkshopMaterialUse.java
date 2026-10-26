package it.SimoSW.model;

import java.math.BigDecimal;

public class WorkshopMaterialUse {
    private final long interventionId;
    private final String materialName, unit;
    private final BigDecimal quantity;

    public WorkshopMaterialUse(long interventionId, String materialName, String unit, BigDecimal quantity) {
        this.interventionId = interventionId;
        this.materialName = materialName;
        this.unit = unit;
        this.quantity = quantity;
    }

    public long getInterventionId() { return interventionId; }
    public String getMaterialName() { return materialName; }
    public String getUnit() { return unit; }
    public BigDecimal getQuantity() { return quantity; }
}
