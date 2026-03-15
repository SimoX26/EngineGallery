package it.SimoSW.model.dao;

import it.SimoSW.model.WarehouseItem;

import java.util.List;
import java.util.Optional;

public interface WarehouseItemDAO {

    List<WarehouseItem> findAll();

    Optional<WarehouseItem> findById(Long id);

    Long save(WarehouseItem item);

    void update(WarehouseItem item);

    void delete(Long id);
}
