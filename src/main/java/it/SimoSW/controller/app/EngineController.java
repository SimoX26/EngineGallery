package it.SimoSW.controller.app;

import it.SimoSW.model.Customer;
import it.SimoSW.model.Engine;
import it.SimoSW.model.EngineStatus;
import it.SimoSW.model.Image;
import it.SimoSW.model.dao.CustomerDAO;
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
    private final EngineDAO engineDAO;
    private final ImageDAO imageDAO;
    private final CustomerDAO customerDAO;

    private final EngineRefGenerator engineRefGenerator;

    public EngineController(EngineDAO engineDAO, ImageDAO imageDAO, CustomerDAO customerDAO, EngineRefGenerator engineRefGenerator) {
        this.engineDAO = engineDAO;
        this.imageDAO = imageDAO;
        this.customerDAO = customerDAO;
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


    public String generateEngineRef() {
        EngineRefGenerator generator = new EngineRefGenerator(engineDAO);
        return generator.generate();
    }



    public Optional<String> getCoverFilenameForEngine(long engineId) {
        return imageDAO.findCoverByEngineId(engineId).map(Image::getFilename);
    }


    public Engine createEngine(EngineBean bean) {

        // =========================
        // 1. VALIDAZIONE BEAN
        // =========================
        if (bean == null) {
            throw new IllegalArgumentException("EngineBean nullo");
        }

        if (bean.getEngineRef() == null || bean.getEngineRef().isBlank()) {
            throw new IllegalArgumentException("engineRef obbligatorio");
        }

        if (bean.getEngineCode() == null || bean.getEngineCode().isBlank()) {
            throw new IllegalArgumentException("engineCode obbligatorio");
        }

        // =========================
        // 2. MOTORE GIÀ ESISTENTE?
        // =========================
        Optional<Engine> existing = engineDAO.findByEngineRef(bean.getEngineRef());

        if (existing.isPresent()) {
            throw new IllegalStateException("Motore con ref " + bean.getEngineRef() + " già esistente");
        }

        // =========================
        // 3. COSTRUZIONE MODEL
        // =========================
        Engine engine = new Engine(
                    bean.getEngineRef(),
                    bean.getEngineCode(),
                    bean.getCustomerId(),
                    LocalDate.parse(bean.getIntakeDate()),
                    EngineStatus.valueOf(bean.getStatus()),
                    bean.getNotes()
                );

        // =========================
        // 4. PERSISTENZA
        // =========================
        return engineDAO.save(engine);
    }


    public Image addImage(String engineRef, String filename) {

        // =========================
        // 1. VALIDAZIONE
        // =========================
        if (engineRef == null || engineRef.isBlank()) {
            throw new IllegalArgumentException("engineRef obbligatorio");
        }

        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("filename obbligatorio");
        }

        // =========================
        // 2. RECUPERO MOTORE
        // =========================
        Engine engine = engineDAO.findByEngineRef(engineRef)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Motore con ref " + engineRef + " non esistente"
                        )
                );

        // =========================
        // 3. COSTRUZIONE MODEL IMAGE
        // =========================
        Image image = new Image(
                engine.getId(),   // relazione DB
                filename,
                null              // uploadedBy (gestibile dopo)
        );

        // =========================
        // 4. PERSISTENZA
        // =========================
        return imageDAO.save(image);
    }

    public boolean deleteEngine(String engineRef) {

        // =========================
        // 1. VALIDAZIONE
        // =========================
        if (engineRef == null || engineRef.isBlank()) {
            throw new IllegalArgumentException("engineRef obbligatorio");
        }

        // =========================
        // 2. RECUPERO MOTORE
        // =========================
        Engine engine = engineDAO.findByEngineRef(engineRef)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Motore con ref " + engineRef + " non esistente"
                        )
                );

        // =========================
        // 3. ELIMINA IMMAGINI ASSOCIATE
        // =========================
        List<Image> images = imageDAO.findAllByEngineId(engine.getId());
        for (Image image : images) {
            imageDAO.delete(image.getId());
        }

        // =========================
        // 4. ELIMINA MOTORE
        // =========================
        return engineDAO.delete(engineRef);
    }


    private EngineBean mapEngineToBean(Engine engine) {

        EngineBean bean = new EngineBean();

        bean.setEngineRef(engine.getEngineRef());
        bean.setEngineCode(engine.getEngineCode());
        bean.setCustomerId(engine.getCustomerId());

        String customerName = customerDAO.findById(engine.getCustomerId())
                .map(Customer::getName)
                .orElse("—");

        bean.setCustomerName(customerName);

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
    private ImageBean mapImageToBean(Image image) {

        ImageBean bean = new ImageBean();

        bean.setFilename(image.getFilename());
        bean.setUploadDate(image.getUploadDate().toString());

        return bean;
    }
}