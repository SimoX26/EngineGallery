package it.SimoSW.controller.app;

import it.SimoSW.model.ReadyDeliveryEngine;
import it.SimoSW.model.dao.ReadyDeliveryDAO;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReadyDeliveryControllerTest {
    @Test
    void validatesAndNormalizesNewReadyDeliveryEngine() {
        StubDAO dao = new StubDAO();
        ReadyDeliveryController controller = new ReadyDeliveryController(dao);

        assertThrows(IllegalArgumentException.class, () -> controller.create(" ", "M1", 1, null, null));
        assertThrows(IllegalArgumentException.class, () -> controller.create("A1", "M1", -1, null, null));
        assertThrows(IllegalArgumentException.class, () -> controller.create("A1", "M1", null, null, null));
        assertEquals(7L, controller.create(" A1 ", " M1 ", 0, " Nota ", " Operatore "));
        assertEquals("A1", dao.engineCode);
        assertEquals("M1", dao.model);
        assertEquals(0, dao.quantity);
        assertEquals("Nota", dao.notes);
        assertEquals("Operatore", dao.createdBy);
    }

    @Test
    void acceptsOnlyOptimizedImageNames() {
        StubDAO dao = new StubDAO();
        ReadyDeliveryController controller = new ReadyDeliveryController(dao);
        assertThrows(IllegalArgumentException.class, () -> controller.setImageFilename(7, "../../bad.jpg"));
        controller.setImageFilename(7, "123e4567-e89b-12d3-a456-426614174000.jpg");
        assertEquals("123e4567-e89b-12d3-a456-426614174000.jpg", dao.imageFilename);
    }

    private static class StubDAO implements ReadyDeliveryDAO {
        String engineCode, model, notes, createdBy, imageFilename;
        int quantity;
        public List<ReadyDeliveryEngine> findAll() { return List.of(); }
        public Optional<ReadyDeliveryEngine> findById(long id) { return Optional.empty(); }
        public long create(String engineCode, String model, int quantity, String notes, String createdBy) {
            this.engineCode = engineCode;
            this.model = model;
            this.quantity = quantity;
            this.notes = notes;
            this.createdBy = createdBy;
            return 7L;
        }
        public void setImageFilename(long id, String filename) { imageFilename = filename; }
        public void delete(long id) { }
    }
}
