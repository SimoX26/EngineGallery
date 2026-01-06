package it.SimoSW.util.bootstrap;

import it.SimoSW.controller.app.AuthenticationController;
import it.SimoSW.controller.app.FolderController;
import it.SimoSW.controller.app.GalleryController;
import it.SimoSW.controller.app.ImageController;
import it.SimoSW.model.dao.*;
import it.SimoSW.model.dao.database.*;

public class ApplicationInitializer {

    /* =========================
       DAO
       ========================= */

    private final ImageDAO imageDAO;
    private final FolderDAO folderDAO;
    private final GalleryDAO galleryDAO;
    private final UserDAO userDAO;
    private final EngineDAO engineDAO;


    /* =========================
       Controller applicativi
       ========================= */

    private final GalleryController galleryController;
    private final ImageController imageController;
    private final FolderController folderController;
    private final AuthenticationController authenticationController;

    public ApplicationInitializer() {

        /* ===== DAO concreti ===== */
        this.imageDAO = new DatabaseImageDAO();
        this.folderDAO = new DatabaseFolderDAO();
        this.galleryDAO = new DatabaseGalleryDAO();
        this.userDAO = new DatabaseUserDAO();
        this.engineDAO = new DatabaseEngineDAO();

        /* ===== Controller ===== */
        this.galleryController = new GalleryController(engineDAO, imageDAO);

        this.imageController = new ImageController(imageDAO);

        this.folderController = new FolderController(galleryDAO);

        this.authenticationController = new AuthenticationController(userDAO);
    }

    /* =========================
       Getter pubblici
       ========================= */

    public GalleryController getGalleryController() {
        return galleryController;
    }

    public ImageController getImageController() {
        return imageController;
    }

    public FolderController getFolderController() {
        return folderController;
    }

    public AuthenticationController getAuthenticationController() {
        return authenticationController;
    }
}
