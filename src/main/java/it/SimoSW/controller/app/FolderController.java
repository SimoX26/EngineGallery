package it.SimoSW.controller.app;

import it.SimoSW.model.Folder;
import it.SimoSW.model.dao.GalleryDAO;

import java.util.List;

public class FolderController {

    private final GalleryDAO galleryDAO;

    public FolderController(GalleryDAO galleryDAO) {
        this.galleryDAO = galleryDAO;
    }

    /* =========================
       Navigazione cartelle
       ========================= */

    /**
     * Restituisce tutte le cartelle radice.
     *
     * @return lista delle cartelle root
     */
    public List<Folder> getRootFolders() {
        return galleryDAO.loadRootFolders();
    }

    /**
     * Restituisce le sottocartelle di una cartella.
     *
     * @param parentFolderId id della cartella padre
     * @return lista delle sottocartelle
     */
    public List<Folder> getSubFolders(long parentFolderId) {
        return galleryDAO.loadSubFolders(parentFolderId);
    }
}
