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
    private final HomeController homeController;
    private final WarehouseController warehouseController;
    private final HydraulicTestController hydraulicTestController;
    private final UserActivityLogController userActivityLogController;
    private final CatalogController catalogController;
    private final WorkshopMaintenanceController workshopMaintenanceController;
    private final VehicleController vehicleController;
    private final ReadyDeliveryController readyDeliveryController;
    private final AndroidPushService androidPushService;

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
        CatalogItemDAO catalogItemDAO = new DatabaseCatalogItemDAO();
        WorkshopMaintenanceDAO workshopMaintenanceDAO = new DatabaseWorkshopMaintenanceDAO();
        VehicleDAO vehicleDAO = new DatabaseVehicleDAO();
        ReadyDeliveryDAO readyDeliveryDAO = new DatabaseReadyDeliveryDAO();
        androidPushService = new AndroidPushService(new DatabaseAndroidPushTokenDAO());

        engineRefGenerator = new EngineRefGenerator(engineDAO);


        /* ===== Controller ===== */
        this.engineController = new EngineController(engineDAO, imageDAO, customerDAO,
                engineRefGenerator, androidPushService);

        this.customerController = new CustomerController(customerDAO);

        this.authenticationController = new AuthenticationController(userDAO);

        this.homeController = new HomeController(
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
        this.catalogController = new CatalogController(catalogItemDAO);
        this.workshopMaintenanceController = new WorkshopMaintenanceController(workshopMaintenanceDAO);
        this.vehicleController = new VehicleController(vehicleDAO);
        this.readyDeliveryController = new ReadyDeliveryController(readyDeliveryDAO);

    }

    /* =========================
       Getter pubblici
       ========================= */

    public EngineController getEngineController() { return engineController; }

    public CustomerController getCustomerController() { return customerController; }

    public AuthenticationController getAuthenticationController() {
        return authenticationController;
    }

    public HomeController getHomeController() {
        return homeController;
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

    public CatalogController getCatalogController() {
        return catalogController;
    }

    public WorkshopMaintenanceController getWorkshopMaintenanceController() { return workshopMaintenanceController; }
    public VehicleController getVehicleController() { return vehicleController; }
    public ReadyDeliveryController getReadyDeliveryController() { return readyDeliveryController; }

    public AndroidPushService getAndroidPushService() { return androidPushService; }
}
