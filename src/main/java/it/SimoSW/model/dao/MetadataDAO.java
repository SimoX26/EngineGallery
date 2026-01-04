package it.SimoSW.model.dao;

import it.SimoSW.model.Metadata;

public interface MetadataDAO {

    void save(Metadata metadata);

    void update(Metadata metadata);

    Metadata findByImageId(long imageId);
}
