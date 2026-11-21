package it.SimoSW.controller.app;

import it.SimoSW.model.WarehouseItem;
import it.SimoSW.model.WarehouseComponentType;
import it.SimoSW.model.WarehouseImage;
import it.SimoSW.model.WarehouseDetails;
import it.SimoSW.model.WarehouseCategory;
import it.SimoSW.model.WarehouseCompatibility;
import it.SimoSW.model.dao.WarehouseItemDAO;
import it.SimoSW.model.dao.WarehouseImageDAO;
import it.SimoSW.model.dao.database.DatabaseWarehouseCatalogDAO;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

public class WarehouseController {

    private final WarehouseItemDAO warehouseItemDAO;
    private final WarehouseImageDAO warehouseImageDAO;
    private final DatabaseWarehouseCatalogDAO catalogDAO;

    public WarehouseController(WarehouseItemDAO warehouseItemDAO, WarehouseImageDAO warehouseImageDAO,
                               DatabaseWarehouseCatalogDAO catalogDAO) {
        this.warehouseItemDAO = warehouseItemDAO;
        this.warehouseImageDAO = warehouseImageDAO;
        this.catalogDAO = catalogDAO;
    }

    public List<WarehouseCategory> getCategories() { return catalogDAO.categories(); }

    public List<WarehouseItem> getAllItems() {
        return warehouseItemDAO.findAll();
    }

    public Optional<WarehouseItem> findById(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return warehouseItemDAO.findById(id);
    }

    public Long createItem(String componentType,
                           String name,
                           String sku,
                           Integer quantity,
                           String location,
                           String notes,
                           String createdBy,
                           WarehouseDetails details) {

        String normalizedName = normalizeRequired(name, "Nome articolo obbligatorio");
        int normalizedQuantity = normalizeQuantity(quantity);
        WarehouseComponentType normalizedComponentType = normalizeComponentType(componentType);

        WarehouseItem item = new WarehouseItem(
                normalizedComponentType,
                normalizedName,
                limitedRequired(sku, 80, "Codice ricambio obbligatorio"),
                normalizedQuantity,
                limitedRequired(location, 120, "Ubicazione obbligatoria"),
                normalizeOptional(notes),
                normalizeOptional(createdBy)
        );

        item.setDetails(normalizeDetails(details));
        validateLotEngine(item);

        return warehouseItemDAO.save(item);
    }

    public void updateItem(Long id,
                           String componentType,
                           String name,
                           String sku,
                           Integer quantity,
                           String location,
                           String notes,
                           WarehouseDetails details) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID articolo non valido");
        }

        if (warehouseItemDAO.findById(id).isEmpty()) {
            throw new IllegalStateException("Articolo non trovato");
        }

        String normalizedName = normalizeRequired(name, "Nome articolo obbligatorio");
        int normalizedQuantity = normalizeQuantity(quantity);
        WarehouseComponentType normalizedComponentType = normalizeComponentType(componentType);

        WarehouseItem item = new WarehouseItem(
                id,
                normalizedComponentType,
                normalizedName,
                limitedRequired(sku, 80, "Codice ricambio obbligatorio"),
                normalizedQuantity,
                limitedRequired(location, 120, "Ubicazione obbligatoria"),
                normalizeOptional(notes),
                null,
                null
        );

        item.setDetails(normalizeDetails(details));
        validateLotEngine(item);

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

    public Map<Long, String> getPreviewFilenames() {
        return warehouseImageDAO.findPreviewFilenames();
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

    private static WarehouseComponentType normalizeComponentType(String value) {
        WarehouseComponentType componentType = WarehouseComponentType.fromValue(value)
                .orElseThrow(() -> new IllegalArgumentException("Tipologia componente obbligatoria"));
        if (componentType == WarehouseComponentType.UNCLASSIFIED) {
            throw new IllegalArgumentException("Seleziona una tipologia componente valida");
        }
        return componentType;
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

    private WarehouseDetails normalizeDetails(WarehouseDetails details) {
        if (details == null || details.getCategoryId() == null) throw new IllegalArgumentException("Categoria obbligatoria");
        WarehouseCategory category = catalogDAO.category(details.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria non valida"));
        boolean injector = category.isInjectorFields();
        Set<String> seen = new HashSet<>();
        List<WarehouseCompatibility> compatibilities = details.getCompatibilities().stream().map(c ->
                new WarehouseCompatibility(limitedRequired(c.getEngineCode(), 120, "Codice motore obbligatorio"), c.isVerified()))
                .filter(c -> seen.add(c.getEngineCode().toUpperCase(java.util.Locale.ROOT)))
                .toList();
        if (compatibilities.size() > 50) throw new IllegalArgumentException("Massimo 50 motori compatibili");
        if (compatibilities.isEmpty()) throw new IllegalArgumentException("Inserisci almeno un motore compatibile");
        return new WarehouseDetails(details.getCategoryId(), limitedRequired(details.getManufacturer(), 120, "Produttore obbligatorio"),
                limitedRequired(details.getOeCode(), 120, "Codice OE obbligatorio"),
                limitedRequired(details.getAlternativeCodes(), 5000, "Codici alternativi obbligatori"),
                limitedRequired(details.getCondition(), 80, "Condizione obbligatoria"),
                limitedRequired(details.getLotCode(), 120, "Lotto obbligatorio"),
                limitedRequired(details.getSourceEngineCode(), 120, "Codice motore di provenienza obbligatorio"),
                limitedRequired(details.getTechnicalNotes(), 10000, "Note tecniche obbligatorie"),
                injector ? limitedRequired(details.getManufacturerCode(), 120, "Codice produttore obbligatorio") : null,
                injector ? limitedRequired(details.getCalibrationCode(), 120, "Codice calibrazione obbligatorio") : null,
                injector ? limitedRequired(details.getSourceCylinder(), 40, "Cilindro di provenienza obbligatorio") : null,
                injector ? limitedRequired(details.getTestStatus(), 80, "Stato collaudo obbligatorio") : null,
                injector ? limitedRequired(details.getRevisionStatus(), 80, "Stato revisione obbligatorio") : null,
                compatibilities);
    }

    private void validateLotEngine(WarehouseItem item) {
        WarehouseDetails details = item.getDetails();
        for (WarehouseItem other : warehouseItemDAO.findAll()) {
            if (item.getId() != null && item.getId().equals(other.getId())) continue;
            WarehouseDetails otherDetails = other.getDetails();
            if (otherDetails != null && details.getLotCode().equalsIgnoreCase(otherDetails.getLotCode())
                    && otherDetails.getSourceEngineCode() != null
                    && !details.getSourceEngineCode().equalsIgnoreCase(otherDetails.getSourceEngineCode())) {
                throw new IllegalArgumentException("Gli articoli dello stesso lotto devono avere lo stesso motore di provenienza");
            }
        }
    }

    private static String limitedRequired(String value, int max, String message) {
        String result = limited(value, max);
        if (result == null) throw new IllegalArgumentException(message);
        return result;
    }

    private static String limited(String value, int max) {
        String result = normalizeOptional(value);
        if (result != null && result.length() > max) throw new IllegalArgumentException("Valore troppo lungo (massimo " + max + " caratteri)");
        return result;
    }
}
