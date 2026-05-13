package it.SimoSW.model.dao;

import it.SimoSW.model.Image;

import java.util.List;
import java.util.Optional;

public interface ImageDAO {

    Image save(Image image);

    boolean delete(long imageId);

    Optional<Image> findById(long imageId);

    List<Image> findAllByEngineId(long engineId);

    Optional<Image> findCoverByEngineId(long engineId);

    List<Image> findLatest(int limit);
}
