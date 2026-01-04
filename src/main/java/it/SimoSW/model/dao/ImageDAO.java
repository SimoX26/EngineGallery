package it.SimoSW.model.dao;

import it.SimoSW.model.EngineStatus;
import it.SimoSW.model.Image;

import java.util.List;

public interface ImageDAO {

    void save(Image image);

    void delete(long imageId);

    Image findById(long imageId);

    List<Image> findByFolder(long folderId);

    List<Image> findByClientName(String clientName);

    List<Image> findByEngineCode(String engineCode);

    List<Image> findByMetadataKeyword(String keyword);

    List<Image> findByEngineStatus(EngineStatus status);
}
