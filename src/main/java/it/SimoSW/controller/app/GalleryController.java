package it.SimoSW.controller.app;

import it.SimoSW.model.Engine;
import it.SimoSW.model.EngineStatus;
import it.SimoSW.model.Image;
import it.SimoSW.model.dao.EngineDAO;
import it.SimoSW.model.dao.ImageDAO;

import java.util.List;
import java.util.Optional;

public class GalleryController {

    private final EngineDAO engineDAO;
    private final ImageDAO imageDAO;

    public GalleryController(EngineDAO engineDAO, ImageDAO imageDAO) {
        this.engineDAO = engineDAO;
        this.imageDAO = imageDAO;
    }

    /* =========================
       Ricerca motori
       ========================= */

    public Optional<Engine> findEngineByCode(String engineCode) {
        return engineDAO.findByEngineCode(engineCode);
    }

    public List<Engine> findEnginesByStatus(EngineStatus status) {
        return engineDAO.findByStatus(status);
    }

    public List<Engine> findEnginesByKeyword(String keyword) {
        return engineDAO.findByKeyword(keyword);
    }

    public List<Engine> getAllEngines() {
        return engineDAO.findAll();
    }

    /* =========================
       Immagini associate
       ========================= */

    public List<Image> getImagesForEngine(long engineId) {
        return imageDAO.findByEngineId(engineId);
    }
}
