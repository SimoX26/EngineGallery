package it.SimoSW.util.bootstrap;

import it.SimoSW.controller.app.*;
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
    private final CustomerDAO customerDAO;


    /* =========================
       Controller applicativi
       ========================= */

    private final GalleryController galleryController;
    private final ImageController imageController;
    private final FolderController folderController;
    private final AuthenticationController authenticationController;
    private final DashboardController dashboardController;


    public ApplicationInitializer() {

        /* ===== DAO concreti ===== */
        this.imageDAO = new DatabaseImageDAO();
        this.folderDAO = new DatabaseFolderDAO();
        this.galleryDAO = new DatabaseGalleryDAO();
        this.userDAO = new DatabaseUserDAO();
        this.engineDAO = new DatabaseEngineDAO();
        this.customerDAO = new DatabaseCustomerDAO();

        /* ===== Controller ===== */
        this.galleryController = new GalleryController(engineDAO, imageDAO);

        this.imageController = new ImageController(imageDAO);

        this.folderController = new FolderController(galleryDAO);

        this.authenticationController = new AuthenticationController(userDAO);

        this.dashboardController = new DashboardController(engineDAO, customerDAO);

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

    public DashboardController getDashboardController() {
        return dashboardController;
    }
}
