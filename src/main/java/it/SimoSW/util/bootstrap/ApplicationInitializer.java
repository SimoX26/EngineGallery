package it.SimoSW.util.bootstrap;

import it.SimoSW.controller.app.*;
import it.SimoSW.model.dao.*;
import it.SimoSW.model.dao.database.*;
import it.SimoSW.model.dao.mock.MockEngineDAO;

public class ApplicationInitializer {

    /* =========================
       Controller applicativi
       ========================= */

    private final EngineController engineController;
    private final AuthenticationController authenticationController;
    private final DashboardController dashboardController;

    public ApplicationInitializer() {

        /* ===== DAO concreti ===== */
        ImageDAO imageDAO = new DatabaseImageDAO();
        UserDAO userDAO = new DatabaseUserDAO();
        EngineDAO engineDAO = new DatabaseEngineDAO();
        CustomerDAO customerDAO = new DatabaseCustomerDAO();


        /* ===== Controller ===== */
        this.engineController = new EngineController(engineDAO, imageDAO);

        this.authenticationController = new AuthenticationController(userDAO);

        this.dashboardController = new DashboardController(engineDAO, customerDAO);

    }

    /* =========================
       Getter pubblici
       ========================= */

    public EngineController getEngineController() { return engineController; }

    public AuthenticationController getAuthenticationController() {
        return authenticationController;
    }

    public DashboardController getDashboardController() {
        return dashboardController;
    }
}
