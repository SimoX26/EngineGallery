package it.SimoSW.model.dao;

import it.SimoSW.model.ReadyDeliveryEngine;
import java.util.List;
import java.util.Optional;

public interface ReadyDeliveryDAO {
    List<ReadyDeliveryEngine> findAll();
    Optional<ReadyDeliveryEngine> findById(long id);
    long create(String engineCode, String model, int quantity, String notes, String createdBy);
    void setImageFilename(long id, String filename);
    void delete(long id);
}
