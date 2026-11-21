package it.SimoSW.controller.app;

import it.SimoSW.model.WarehouseCategory;
import it.SimoSW.model.WarehouseCompatibility;
import it.SimoSW.model.WarehouseDetails;
import it.SimoSW.model.WarehouseItem;
import it.SimoSW.model.WarehouseComponentType;
import it.SimoSW.model.dao.WarehouseItemDAO;
import it.SimoSW.model.dao.database.DatabaseWarehouseCatalogDAO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WarehouseControllerTest {
    private final FakeWarehouseItemDAO items = new FakeWarehouseItemDAO();
    private final WarehouseController controller = new WarehouseController(items, null, new FakeCategories());

    @Test
    void createStoresRequiredDetailsAndUnverifiedCompatibility() {
        Long id = controller.createItem("INJECTORS", "Iniettore", "BOSCH-1", 4,
                "A-02-03", "note", "simone", details(1L));

        assertEquals(42L, id);
        assertEquals("KIT-0001", items.saved.getDetails().getLotCode());
        assertEquals(4, items.saved.getQuantity());
        assertEquals("BOSCH-C", items.saved.getDetails().getCalibrationCode());
        assertEquals(1, items.saved.getDetails().getCompatibilities().size());
        assertFalse(items.saved.getDetails().getCompatibilities().get(0).isVerified());
    }

    @Test
    void createRejectsMissingNewDataBeforeSaving() {
        WarehouseDetails incomplete = new WarehouseDetails(1L, "Bosch", null, "ALT-1", "Usato",
                "KIT-0001", "MOT-1", "note", "BOSCH-1", "BOSCH-C", "1", "Ok", "Revisionato",
                List.of(new WarehouseCompatibility("MOT-2", false)));

        assertThrows(IllegalArgumentException.class, () -> controller.createItem("INJECTORS", "Iniettore",
                "BOSCH-1", 4, "A-02-03", null, "simone", incomplete));
        assertEquals(null, items.saved);
    }

    @Test
    void nonInjectorCategoryDoesNotStoreInjectorFields() {
        controller.createItem("OTHER", "Rail", "RAIL-1", 1, "A-02-04", null, "simone", details(2L));

        assertNotNull(items.saved);
        assertEquals(null, items.saved.getDetails().getCalibrationCode());
        assertEquals("KIT-0001", items.saved.getDetails().getLotCode());
    }

    @Test
    void categoryRenameDoesNotChangeInjectorValidation() {
        WarehouseDetails missingCalibration = new WarehouseDetails(1L, "Bosch", "OE-1", "ALT-1", "Usato",
                "KIT-0001", "MOT-1", "note", "BOSCH-1", null, "1", "Ok", "Revisionato",
                List.of(new WarehouseCompatibility("MOT-2", true)));

        assertThrows(IllegalArgumentException.class, () -> controller.createItem("INJECTORS", "Iniettore",
                "BOSCH-1", 4, "A-02-03", null, "simone", missingCalibration));
    }

    @Test
    void lotRejectsDifferentSourceEngineWithoutChangingExistingItem() {
        WarehouseItem existing = new WarehouseItem(7L, WarehouseComponentType.OTHER, "Rail", "RAIL-1", 1,
                "A-02-04", null, null, null);
        existing.setDetails(details(2L));
        items.existing = List.of(existing);
        WarehouseDetails conflicting = new WarehouseDetails(2L, "Bosch", "OE-1", "ALT-1", "Usato",
                "KIT-0001", "ALTRO-MOTORE", "note", null, null, null, null, null,
                List.of(new WarehouseCompatibility("MOT-2", false)));

        assertThrows(IllegalArgumentException.class, () -> controller.createItem("OTHER", "Pompa",
                "POMPA-1", 1, "B-01-01", null, "simone", conflicting));
        assertEquals(null, items.saved);
        assertEquals(1, existing.getQuantity());
    }

    private static WarehouseDetails details(Long categoryId) {
        return new WarehouseDetails(categoryId, "Bosch", "OE-1", "ALT-1", "Usato", "KIT-0001",
                "MOT-1", "note tecniche", "BOSCH-1", "BOSCH-C", "1", "Ok", "Revisionato",
                List.of(new WarehouseCompatibility("MOT-2", false), new WarehouseCompatibility("mot-2", true)));
    }

    private static class FakeCategories extends DatabaseWarehouseCatalogDAO {
        @Override
        public Optional<WarehouseCategory> category(Long id) {
            if (id == 1L) return Optional.of(new WarehouseCategory(1L, "Alimentazione", "Iniettori rinominati", true));
            if (id == 2L) return Optional.of(new WarehouseCategory(2L, "Alimentazione", "Rail", false));
            return Optional.empty();
        }
    }

    private static class FakeWarehouseItemDAO implements WarehouseItemDAO {
        private WarehouseItem saved;
        private List<WarehouseItem> existing = List.of();

        public List<WarehouseItem> findAll() { return existing; }
        public List<WarehouseItem> findLatest(int limit) { return List.of(); }
        public Optional<WarehouseItem> findById(Long id) { return Optional.empty(); }
        public Long save(WarehouseItem item) { saved = item; return 42L; }
        public void update(WarehouseItem item) { saved = item; }
        public void delete(Long id) { }
        public int countAll() { return 0; }
        public int sumTotalQuantity() { return 0; }
        public int countOutOfStock() { return 0; }
    }
}
