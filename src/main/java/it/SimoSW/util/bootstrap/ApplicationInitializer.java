package it.SimoSW.util.bootstrap;

import it.SimoSW.controller.app.*;
import it.SimoSW.model.dao.*;
import it.SimoSW.model.dao.database.*;
import it.SimoSW.util.generator.EngineRefGenerator;

public class ApplicationInitializer {

    /* =========================
       Controller applicativi
       ========================= */

    private final EngineController engineController;
    private final CustomerController customerController;
    private final AuthenticationController authenticationController;
    private final DashboardController dashboardController;
    private final WarehouseController warehouseController;
    private final HydraulicTestController hydraulicTestController;
    private final UserActivityLogController userActivityLogController;

    private final EngineRefGenerator engineRefGenerator;

    public ApplicationInitializer() {

        /* ===== DAO concreti ===== */
        ImageDAO imageDAO = new DatabaseImageDAO();
        UserDAO userDAO = new DatabaseUserDAO();
        EngineDAO engineDAO = new DatabaseEngineDAO();
        CustomerDAO customerDAO = new DatabaseCustomerDAO();
        WarehouseItemDAO warehouseItemDAO = new DatabaseWarehouseItemDAO();
        WarehouseImageDAO warehouseImageDAO = new DatabaseWarehouseImageDAO();
        HydraulicTestDAO hydraulicTestDAO = new DatabaseHydraulicTestDAO();
        UserActivityLogDAO userActivityLogDAO = new DatabaseUserActivityLogDAO();

        engineRefGenerator = new EngineRefGenerator(engineDAO);


        /* ===== Controller ===== */
        this.engineController = new EngineController(engineDAO, imageDAO, customerDAO, engineRefGenerator);

        this.customerController = new CustomerController(customerDAO);

        this.authenticationController = new AuthenticationController(userDAO);

        this.dashboardController = new DashboardController(
                engineDAO,
                customerDAO,
                warehouseItemDAO,
                imageDAO,
                warehouseImageDAO,
                userDAO,
                userActivityLogDAO
        );

        this.warehouseController = new WarehouseController(warehouseItemDAO, warehouseImageDAO);
        this.hydraulicTestController = new HydraulicTestController(hydraulicTestDAO);
        this.userActivityLogController = new UserActivityLogController(userActivityLogDAO);

    }

    /* =========================
       Getter pubblici
       ========================= */

    public EngineController getEngineController() { return engineController; }

    public CustomerController getCustomerController() { return customerController; }

    public AuthenticationController getAuthenticationController() {
        return authenticationController;
    }

    public DashboardController getDashboardController() {
        return dashboardController;
    }

    public WarehouseController getWarehouseController() {
        return warehouseController;
    }

    public HydraulicTestController getHydraulicTestController() {
        return hydraulicTestController;
    }

    public UserActivityLogController getUserActivityLogController() {
        return userActivityLogController;
    }
}
