package it.SimoSW.controller.app;

import it.SimoSW.model.dao.CustomerDAO;
import it.SimoSW.model.dao.EngineDAO;

public class DashboardController {

    private final EngineDAO engineDAO;
    private final CustomerDAO customerDAO;

    public DashboardController(EngineDAO engineDAO, CustomerDAO customerDAO) {
        this.engineDAO = engineDAO;
        this.customerDAO = customerDAO;
    }

    public int getClientiConMotoriAttivi() {
        return customerDAO.countClientiConMotoriInOfficina();
    }


    public int getMotoriInOfficina() {
        return engineDAO.countMotoriInOfficina();
    }


    public int getMotoriInLavorazione() {
        return engineDAO.countMotoriInLavorazione();
    }


    public int getMotoriConsegnatiUltimaSettimana() {
        return engineDAO.countMotoriConsegnatiUltimi7Giorni();
    }

}
