package it.SimoSW.controller.app;

import it.SimoSW.model.CatalogItem;
import it.SimoSW.model.dao.CatalogItemDAO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogControllerTest {

    @Test
    void getAllItemsDelegatesToDao() {
        CatalogItem expected = catalogItem(1L, "FIAT");
        FakeCatalogItemDAO dao = new FakeCatalogItemDAO();
        dao.items = List.of(expected);
        CatalogController controller = new CatalogController(dao);

        List<CatalogItem> result = controller.getAllItems();

        assertEquals(List.of(expected), result);
        assertEquals(1, dao.findAllCalls);
    }

    @Test
    void findByIdDelegatesValidIdToDao() {
        CatalogItem expected = catalogItem(7L, "PEUGEOT");
        FakeCatalogItemDAO dao = new FakeCatalogItemDAO();
        dao.itemById = Optional.of(expected);
        CatalogController controller = new CatalogController(dao);

        Optional<CatalogItem> result = controller.findById(7L);

        assertSame(expected, result.orElseThrow());
        assertEquals(1, dao.findByIdCalls);
        assertEquals(7L, dao.requestedId);
    }

    @Test
    void findByIdRejectsNonPositiveIdsWithoutCallingDao() {
        FakeCatalogItemDAO dao = new FakeCatalogItemDAO();
        CatalogController controller = new CatalogController(dao);

        assertTrue(controller.findById(0L).isEmpty());
        assertTrue(controller.findById(-1L).isEmpty());
        assertEquals(0, dao.findByIdCalls);
    }

    private static CatalogItem catalogItem(long id, String engineModel) {
        return new CatalogItem(
                id,
                new BigDecimal("86.50"),
                engineModel,
                1998,
                16,
                "TEST-001"
        );
    }

    private static final class FakeCatalogItemDAO implements CatalogItemDAO {
        private List<CatalogItem> items = List.of();
        private Optional<CatalogItem> itemById = Optional.empty();
        private int findAllCalls;
        private int findByIdCalls;
        private long requestedId;

        @Override
        public List<CatalogItem> findAll() {
            findAllCalls++;
            return items;
        }

        @Override
        public Optional<CatalogItem> findById(long id) {
            findByIdCalls++;
            requestedId = id;
            return itemById;
        }
    }
}
