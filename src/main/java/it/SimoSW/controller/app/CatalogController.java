package it.SimoSW.controller.app;

import it.SimoSW.model.CatalogItem;
import it.SimoSW.model.dao.CatalogItemDAO;

import java.util.List;
import java.util.Optional;

public class CatalogController {

    private final CatalogItemDAO catalogItemDAO;

    public CatalogController(CatalogItemDAO catalogItemDAO) {
        this.catalogItemDAO = catalogItemDAO;
    }

    public List<CatalogItem> getAllItems() {
        return catalogItemDAO.findAll();
    }

    public Optional<CatalogItem> findById(long id) {
        if (id <= 0) {
            return Optional.empty();
        }
        return catalogItemDAO.findById(id);
    }
}
