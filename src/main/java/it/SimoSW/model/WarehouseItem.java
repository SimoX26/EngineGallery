package it.SimoSW.model;

import java.util.Objects;

public class WarehouseItem {

    private Long id;
    private final String name;
    private final String sku;
    private final int quantity;
    private final String location;
    private final String notes;

    public WarehouseItem(String name,
                         String sku,
                         int quantity,
                         String location,
                         String notes) {

        this.name = Objects.requireNonNull(name, "Il nome articolo è obbligatorio");
        this.sku = sku;
        this.quantity = quantity;
        this.location = location;
        this.notes = notes;
    }

    public WarehouseItem(Long id,
                         String name,
                         String sku,
                         int quantity,
                         String location,
                         String notes) {

        this(name, sku, quantity, location, notes);
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getLocation() {
        return location;
    }

    public String getNotes() {
        return notes;
    }
}
