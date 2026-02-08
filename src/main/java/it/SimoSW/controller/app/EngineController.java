package it.SimoSW.controller.app;

import it.SimoSW.model.Engine;
import it.SimoSW.model.EngineStatus;
import it.SimoSW.model.Image;
import it.SimoSW.model.dao.EngineDAO;
import it.SimoSW.model.dao.ImageDAO;
import it.SimoSW.util.bean.EngineDetailBean;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class EngineController {

    private final EngineDAO engineDAO;
    private final ImageDAO imageDAO;

    public EngineController(EngineDAO engineDAO, ImageDAO imageDAO) {
        this.engineDAO = engineDAO;
        this.imageDAO = imageDAO;
    }

    /* =========================
       Ricerca motori
       ========================= */

    public Optional<Engine> findEngineByRef(String engineRef) {
        if (engineRef == null || engineRef.isBlank()) {
            return Optional.empty();
        }
        return engineDAO.findByEngineRef(engineRef.trim());
    }

    /**
     * NOTA: engine_code NON è univoco, quindi il risultato è una LISTA.
     */
    public List<Engine> findEnginesByCode(String engineCode) {
        if (engineCode == null || engineCode.isBlank()) {
            return List.of();
        }
        return engineDAO.findByEngineCode(engineCode.trim());
    }

    public List<Engine> findEnginesByStatus(EngineStatus status) {
        if (status == null) {
            return List.of();
        }
        return engineDAO.findByStatus(status);
    }

    /**
     * Ricerca testuale (engine_ref, engine_code, note, customer...)
     */
    public List<Engine> searchEngines(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return engineDAO.search(keyword.trim());
    }

    public List<Engine> getAllEngines() {
        return engineDAO.findAll();
    }

    public EngineDetailBean getEngineDetail(long engineId) {
        Optional<Engine> engineOpt = engineDAO.findById(engineId);
        if (engineOpt.isEmpty()) {
            return null;
        }

        Engine engine = engineOpt.get();
        List<Image> images = imageDAO.findAllByEngineId(engineId);

        return new EngineDetailBean(engine, images);
    }

    public Optional<String> getCoverFilenameForEngine(long engineId) {
        return imageDAO.findCoverByEngineId(engineId).map(Image::getFilename);
    }

    public Engine getOrCreateEngine(String customer, String engineCode) {

        Optional<Engine> existing = engineDAO.findByCustomerAndEngineCode(customer, engineCode);

        if (existing.isPresent()) {
            return existing.get();
        }


        /*
        long customerId = customerDAO
                .findByName(customer)
                .orElseThrow(() ->
                        new IllegalArgumentException("Cliente non trovato: " + customer)
                )
                .getId();
        */

        long customerId = 0;

        Engine engine = new Engine(
                0L,                     // id temporaneo (non ancora persistito)
                null,                   // engineRef non ancora assegnato
                engineCode,
                customerId,
                LocalDate.now(),         // intakeDate
                EngineStatus.WAITING,    // stato iniziale
                null,                   // deliveryDate
                null                    // note
        );

        // persist → ottieni ID
        engine = engineDAO.save(engine);

        // ORA puoi assegnare engineRef (una sola volta)
        engine.assignEngineRef("MOTORE-" + engine.getId());

        engineDAO.update(engine);

        return engine;
    }
}