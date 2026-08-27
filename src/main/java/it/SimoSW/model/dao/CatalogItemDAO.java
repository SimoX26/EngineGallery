package it.SimoSW.model.dao;

import it.SimoSW.model.CatalogItem;

import java.util.List;
import java.util.Optional;

public interface CatalogItemDAO {

    List<CatalogItem> findAll();

    Optional<CatalogItem> findById(long id);
}
