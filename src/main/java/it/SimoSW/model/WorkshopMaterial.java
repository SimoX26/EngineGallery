package it.SimoSW.model;

import java.math.BigDecimal;

public class WorkshopMaterial {
    private final long id;
    private final String name, category, unit, location;
    private final BigDecimal quantity, minimumQuantity;

    public WorkshopMaterial(long id, String name, String category, String unit, String location,
                            BigDecimal quantity, BigDecimal minimumQuantity) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.location = location;
        this.quantity = quantity;
        this.minimumQuantity = minimumQuantity;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getCategoryLabel() {
        return switch (category) {
            case "FILTRO" -> "Filtri";
            case "DETERGENTE" -> "Detergenti";
            case "LUBRIFICANTE" -> "Lubrificanti";
            case "LIQUIDO" -> "Liquidi";
            case "RICAMBIO" -> "Ricambi";
            default -> "Altro";
        };
    }
    public String getUnit() { return unit; }
    public String getLocation() { return location; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getMinimumQuantity() { return minimumQuantity; }
    public boolean isLowStock() { return quantity.compareTo(minimumQuantity) <= 0; }
}
