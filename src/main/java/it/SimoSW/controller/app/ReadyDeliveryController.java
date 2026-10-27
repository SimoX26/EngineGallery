package it.SimoSW.controller.app;

import it.SimoSW.model.ReadyDeliveryEngine;
import it.SimoSW.model.dao.ReadyDeliveryDAO;
import java.util.List;
import java.util.Optional;

public class ReadyDeliveryController {
    private final ReadyDeliveryDAO dao;

    public ReadyDeliveryController(ReadyDeliveryDAO dao) { this.dao = dao; }
    public List<ReadyDeliveryEngine> getEngines() { return dao.findAll(); }
    public Optional<ReadyDeliveryEngine> findById(long id) {
        return id > 0 ? dao.findById(id) : Optional.empty();
    }

    public long create(String engineCode, String model, Integer quantity, String notes, String createdBy) {
        if (quantity == null || quantity < 0)
            throw new IllegalArgumentException("Quantità non valida");
        return dao.create(required(engineCode, 80, "Codice motore"), required(model, 120, "Modello"),
                quantity, optional(notes, 2000, "Note"), optional(createdBy, 100, "Operatore"));
    }

    public void setImageFilename(long id, String filename) {
        if (id <= 0 || filename == null || !filename.matches("[0-9a-fA-F-]{36}\\.jpg"))
            throw new IllegalArgumentException("Foto non valida");
        dao.setImageFilename(id, filename);
    }

    public void delete(long id) {
        if (id > 0) dao.delete(id);
    }

    private static String required(String value, int maxLength, String label) {
        String normalized = optional(value, maxLength, label);
        if (normalized == null) throw new IllegalArgumentException(label + " obbligatorio");
        return normalized;
    }

    private static String optional(String value, int maxLength, String label) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.length() > maxLength) throw new IllegalArgumentException(label + " troppo lungo");
        return normalized.isEmpty() ? null : normalized;
    }
}
