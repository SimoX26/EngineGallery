package it.SimoSW.model.dao;

import it.SimoSW.model.WarehouseItem;

import java.util.List;
import java.util.Optional;

public interface WarehouseItemDAO {

    List<WarehouseItem> findAll();

    List<WarehouseItem> findLatest(int limit);

    Optional<WarehouseItem> findById(Long id);

    Long save(WarehouseItem item);

    void update(WarehouseItem item);

    void delete(Long id);

    int countAll();

    int sumTotalQuantity();

    int countOutOfStock();
}
