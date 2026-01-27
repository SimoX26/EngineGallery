package it.SimoSW;

import it.SimoSW.model.Image;
import it.SimoSW.model.dao.ImageDAO;
import it.SimoSW.model.dao.database.DatabaseImageDAO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseImageDAOTest {

    private final ImageDAO imageDAO = new DatabaseImageDAO();

    @Test
    void saveImage_shouldPersistImage() {

        // GIVEN
        Image image = new Image(1, "test_image.jpg");

        // WHEN
        Image saved = imageDAO.save(image);

        // THEN
        assertNotNull(saved);
        assertTrue(saved.getId() > 0);
        assertEquals(1, saved.getEngineId());
        assertEquals("test_image.jpg", saved.getFilename());
    }
}
