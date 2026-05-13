package it.SimoSW.model.dao;

import it.SimoSW.model.WarehouseImage;

import java.util.List;
import java.util.Optional;

public interface WarehouseImageDAO {

    WarehouseImage save(WarehouseImage image);

    boolean delete(long imageId);

    Optional<WarehouseImage> findById(long imageId);

    List<WarehouseImage> findAllByWarehouseItemId(long itemId);

    List<WarehouseImage> findLatest(int limit);
}
