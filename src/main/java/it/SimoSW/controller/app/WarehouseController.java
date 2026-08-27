package it.SimoSW.controller.app;

import it.SimoSW.model.WarehouseItem;
import it.SimoSW.model.WarehouseImage;
import it.SimoSW.model.dao.WarehouseItemDAO;
import it.SimoSW.model.dao.WarehouseImageDAO;

import java.util.List;
import java.util.Optional;

public class WarehouseController {

    private final WarehouseItemDAO warehouseItemDAO;
    private final WarehouseImageDAO warehouseImageDAO;

    public WarehouseController(WarehouseItemDAO warehouseItemDAO, WarehouseImageDAO warehouseImageDAO) {
        this.warehouseItemDAO = warehouseItemDAO;
        this.warehouseImageDAO = warehouseImageDAO;
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
                           String notes,
                           String createdBy) {

        String normalizedName = normalizeRequired(name, "Nome articolo obbligatorio");
        int normalizedQuantity = normalizeQuantity(quantity);

        WarehouseItem item = new WarehouseItem(
                normalizedName,
                normalizeOptional(sku),
                normalizedQuantity,
                normalizeOptional(location),
                normalizeOptional(notes),
                normalizeOptional(createdBy)
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

    public List<WarehouseImage> findImagesByItemId(Long itemId) {
        if (itemId == null || itemId <= 0) {
            return List.of();
        }
        return warehouseImageDAO.findAllByWarehouseItemId(itemId);
    }

    public WarehouseImage addImage(Long itemId, String filename) {
        if (itemId == null || itemId <= 0) {
            throw new IllegalArgumentException("ID articolo non valido");
        }
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Nome file immagine non valido");
        }
        if (warehouseItemDAO.findById(itemId).isEmpty()) {
            throw new IllegalStateException("Articolo non trovato");
        }
        WarehouseImage image = new WarehouseImage(itemId, filename.trim(), null);
        return warehouseImageDAO.save(image);
    }

    public boolean deleteImageByFilename(Long itemId, String filename) {
        if (itemId == null || itemId <= 0 || filename == null || filename.isBlank()) {
            return false;
        }
        List<WarehouseImage> images = warehouseImageDAO.findAllByWarehouseItemId(itemId);
        for (WarehouseImage image : images) {
            if (image.getFilename().equals(filename.trim())) {
                return warehouseImageDAO.delete(image.getId());
            }
        }
        return false;
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
