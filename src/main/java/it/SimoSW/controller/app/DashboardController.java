package it.SimoSW.controller.app;

import it.SimoSW.model.EngineStatus;
import it.SimoSW.model.dao.CustomerDAO;
import it.SimoSW.model.dao.EngineDAO;

import java.time.LocalDate;

public class DashboardController {

    private final EngineDAO engineDAO;
    private final CustomerDAO customerDAO;

    public DashboardController(EngineDAO engineDAO, CustomerDAO customerDAO) {
        this.engineDAO = engineDAO;
        this.customerDAO = customerDAO;
    }

    /* =========================
       KPI Clienti
       ========================= */

    public int getClientiConMotoriInOfficina() {
        // questo metodo deve esistere nel CustomerDAO (o lo aggiorniamo dopo)
        return customerDAO.countClientiConMotoriInOfficina();
    }

    /* =========================
       KPI Motori
       ========================= */

    public int getMotoriInOfficina() {
        return engineDAO.countInWorkshop();
    }

    public int getWorkInProgressEngines() {
        return engineDAO.countByStatus(EngineStatus.WORK_IN_PROGRESS);
    }

    public int getMotoriConsegnatiUltimaSettimana() {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(7);
        return engineDAO.countDeliveredBetween(from, to);
    }

    /* =========================
       Lista ultimi motori (TODO)
       ========================= */
    public int listaUltimiMotori() {
        // qui non hai ancora un metodo DAO adatto.
        // Quando lo aggiungi (es. findLatest(int limit)) potrai implementarlo.
        return 0;
    }
}