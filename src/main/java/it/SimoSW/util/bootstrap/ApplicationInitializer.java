package it.SimoSW.util.bootstrap;

import it.SimoSW.controller.app.*;
import it.SimoSW.model.dao.*;
import it.SimoSW.model.dao.database.*;

public class ApplicationInitializer {

    /* =========================
       DAO
       ========================= */

    private final ImageDAO imageDAO;
    private final GalleryDAO galleryDAO;
    private final UserDAO userDAO;
    private final EngineDAO engineDAO;
    private final CustomerDAO customerDAO;


    /* =========================
       Controller applicativi
       ========================= */

    private final EngineController galleryController;
    private final ImageController imageController;
    private final AuthenticationController authenticationController;
    private final DashboardController dashboardController;


    public ApplicationInitializer() {

        /* ===== DAO concreti ===== */
        this.imageDAO = new DatabaseImageDAO();
        this.galleryDAO = new DatabaseGalleryDAO();
        this.userDAO = new DatabaseUserDAO();
        this.engineDAO = new DatabaseEngineDAO();
        this.customerDAO = new DatabaseCustomerDAO();

        /* ===== Controller ===== */
        this.galleryController = new EngineController(engineDAO, imageDAO);

        this.imageController = new ImageController(imageDAO);

        this.authenticationController = new AuthenticationController(userDAO);

        this.dashboardController = new DashboardController(engineDAO, customerDAO);

    }

    /* =========================
       Getter pubblici
       ========================= */

    public EngineController getGalleryController() {
        return galleryController;
    }

    public ImageController getImageController() {
        return imageController;
    }

    public AuthenticationController getAuthenticationController() {
        return authenticationController;
    }

    public DashboardController getDashboardController() {
        return dashboardController;
    }
}
