package it.SimoSW.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class WarehouseItem {

    private static final DateTimeFormatter CREATED_AT_FORMATTER =
            DateTimeFormatter.ofPattern("dd / MM / yyyy · HH:mm");

    private Long id;
    private final String name;
    private final String sku;
    private final int quantity;
    private final String location;
    private final String notes;
    private final LocalDateTime createdAt;
    private final String createdBy;

    public WarehouseItem(String name,
                         String sku,
                         int quantity,
                         String location,
                         String notes) {

        this(name, sku, quantity, location, notes, null);
    }

    public WarehouseItem(String name,
                         String sku,
                         int quantity,
                         String location,
                         String notes,
                         String createdBy) {

        this.name = Objects.requireNonNull(name, "Il nome articolo è obbligatorio");
        this.sku = sku;
        this.quantity = quantity;
        this.location = location;
        this.notes = notes;
        this.createdAt = null;
        this.createdBy = normalizeOptional(createdBy);
    }

    public WarehouseItem(Long id,
                         String name,
                         String sku,
                         int quantity,
                         String location,
                         String notes) {

        this(id, name, sku, quantity, location, notes, null, null);
    }

    public WarehouseItem(Long id,
                         String name,
                         String sku,
                         int quantity,
                         String location,
                         String notes,
                         LocalDateTime createdAt,
                         String createdBy) {

        this.name = Objects.requireNonNull(name, "Il nome articolo è obbligatorio");
        this.sku = sku;
        this.quantity = quantity;
        this.location = location;
        this.notes = notes;
        this.id = id;
        this.createdAt = createdAt;
        this.createdBy = normalizeOptional(createdBy);
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getCreatedAtLabel() {
        return createdAt != null ? CREATED_AT_FORMATTER.format(createdAt) : "";
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
