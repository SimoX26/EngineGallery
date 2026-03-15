package it.SimoSW.controller.app;

import it.SimoSW.model.WarehouseItem;
import it.SimoSW.model.dao.WarehouseItemDAO;

import java.util.List;
import java.util.Optional;

public class WarehouseController {

    private final WarehouseItemDAO warehouseItemDAO;

    public WarehouseController(WarehouseItemDAO warehouseItemDAO) {
        this.warehouseItemDAO = warehouseItemDAO;
    }

    public List<WarehouseItem> getAllItems() {
        return warehouseItemDAO.findAll();
    }

    public Optional<WarehouseItem> findById(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return warehouseItemDAO.findById(id);
    }

    public Long createItem(String name,
                           String sku,
                           Integer quantity,
                           String location,
                           String notes) {

        String normalizedName = normalizeRequired(name, "Nome articolo obbligatorio");
        int normalizedQuantity = normalizeQuantity(quantity);

        WarehouseItem item = new WarehouseItem(
                normalizedName,
                normalizeOptional(sku),
                normalizedQuantity,
                normalizeOptional(location),
                normalizeOptional(notes)
        );

        return warehouseItemDAO.save(item);
    }

    public void updateItem(Long id,
                           String name,
                           String sku,
                           Integer quantity,
                           String location,
                           String notes) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID articolo non valido");
        }

        if (warehouseItemDAO.findById(id).isEmpty()) {
            throw new IllegalStateException("Articolo non trovato");
        }

        String normalizedName = normalizeRequired(name, "Nome articolo obbligatorio");
        int normalizedQuantity = normalizeQuantity(quantity);

        WarehouseItem item = new WarehouseItem(
                id,
                normalizedName,
                normalizeOptional(sku),
                normalizedQuantity,
                normalizeOptional(location),
                normalizeOptional(notes)
        );

        warehouseItemDAO.update(item);
    }

    public void deleteItem(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID articolo non valido");
        }

        if (warehouseItemDAO.findById(id).isEmpty()) {
            throw new IllegalStateException("Articolo non trovato");
        }

        warehouseItemDAO.delete(id);
    }

    private static int normalizeQuantity(Integer quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("Quantita obbligatoria");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantita non valida");
        }
        return quantity;
    }

    private static String normalizeRequired(String value, String errorMessage) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new IllegalArgumentException(errorMessage);
        }
        return normalized;
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
