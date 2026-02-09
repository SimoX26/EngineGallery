package it.SimoSW.controller.app;

import it.SimoSW.model.Engine;
import it.SimoSW.model.EngineStatus;
import it.SimoSW.model.Image;
import it.SimoSW.model.dao.EngineDAO;
import it.SimoSW.model.dao.ImageDAO;
import it.SimoSW.util.bean.EngineBean;
import it.SimoSW.util.bean.EngineDetailBean;
import it.SimoSW.util.bean.ImageBean;
import it.SimoSW.util.generator.EngineRefGenerator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class EngineController {
    private final EngineRefGenerator engineRefGenerator;

    private final EngineDAO engineDAO;
    private final ImageDAO imageDAO;

    public EngineController(EngineDAO engineDAO, ImageDAO imageDAO, EngineRefGenerator engineRefGenerator) {
        this.engineDAO = engineDAO;
        this.imageDAO = imageDAO;
        this.engineRefGenerator = engineRefGenerator;
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

    public EngineDetailBean getEngineDetail(String engineRef) {

        // Recupero motore tramite business key
        Optional<Engine> engineOpt = engineDAO.findByEngineRef(engineRef);
        if (engineOpt.isEmpty()) {
            return null;
        }

        Engine engine = engineOpt.get();

        // Uso l'ID tecnico SOLO internamente
        long engineId = engine.getId();

        // Recupero immagini associate
        List<Image> images = imageDAO.findAllByEngineId(engineId);

        // Mapping
        EngineBean engineBean = mapEngineToBean(engine);
        List<ImageBean> imageBeans = images.stream()
                .map(this::mapImageToBean)
                .toList();

        EngineDetailBean detailBean = new EngineDetailBean();
        detailBean.setEngine(engineBean);
        detailBean.setImages(imageBeans);

        return detailBean;
    }

    public String setEngineDetail(EngineDetailBean detailBean) {

        // 0. validazione dell'input
        if (detailBean == null || detailBean.getEngine() == null) {
            throw new IllegalArgumentException("EngineDetailBean non valido");
        }

        if (detailBean.getImages() == null || detailBean.getImages().isEmpty()) {
            throw new IllegalArgumentException("Deve esserci almeno un'immagine");
        }

        // 1. genera engineRef
        String engineRef = engineRefGenerator.generate();

        // 2. assegna al bean
        detailBean.getEngine().setEngineRef(engineRef);

        // 3. salva motore
        long engineId = engineDAO.save(mapBeanToEngine(detailBean.getEngine()));

        // 4. salva immagini
        for (ImageBean img : detailBean.getImages()) {
            imageDAO.save(img.getFilename(), engineId);
        }

        return engineRef;
    }



    public Optional<String> getCoverFilenameForEngine(long engineId) {
        return imageDAO.findCoverByEngineId(engineId).map(Image::getFilename);
    }





    private EngineBean mapEngineToBean(Engine engine) {

        EngineBean bean = new EngineBean();

        bean.setEngineRef(engine.getEngineRef());
        bean.setEngineCode(engine.getEngineCode());
        bean.setCustomerId(engine.getCustomerId());

        bean.setStatus(engine.getStatus().name());
        bean.setIntakeDate(engine.getIntakeDate().toString());

        bean.setDeliveryDate(
                engine.getDeliveryDate() != null
                        ? engine.getDeliveryDate().toString()
                        : null
        );

        bean.setNotes(engine.getNotes());

        return bean;
    }
    private Engine mapBeanToEngine(EngineBean bean) {

        if (bean == null) {
            throw new IllegalArgumentException("EngineBean nullo");
        }

        if (bean.getEngineRef() == null || bean.getEngineRef().isBlank()) {
            throw new IllegalArgumentException("engineRef mancante");
        }

        if (bean.getEngineCode() == null || bean.getEngineCode().isBlank()) {
            throw new IllegalArgumentException("engineCode mancante");
        }

        if (bean.getCustomerId() <= 0) {
            throw new IllegalArgumentException("customerId non valido");
        }

        // Conversioni
        EngineStatus status = bean.getStatus() != null
                ? EngineStatus.valueOf(bean.getStatus())
                : EngineStatus.WAITING;

        LocalDate intakeDate = bean.getIntakeDate() != null
                ? LocalDate.parse(bean.getIntakeDate())
                : LocalDate.now();

        // Costruzione dell'Entity (NUOVO Engine)
        return new Engine(
                bean.getEngineRef(),     // business key
                bean.getEngineCode(),    // codice motore
                bean.getCustomerId(),    // cliente
                intakeDate,              // data ingresso
                status,                  // stato iniziale
                bean.getNotes()          // note (può essere null)
        );
    }

    private ImageBean mapImageToBean(Image image) {

        ImageBean bean = new ImageBean();

        bean.setFilename(image.getFilename());
        bean.setUploadDate(image.getUploadDate().toString());

        return bean;
    }
}