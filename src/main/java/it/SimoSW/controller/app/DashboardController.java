package it.SimoSW.controller.app;

import it.SimoSW.model.EngineStatus;
import it.SimoSW.model.WarehouseItem;
import it.SimoSW.model.dao.CustomerDAO;
import it.SimoSW.model.dao.EngineDAO;
import it.SimoSW.model.Engine;
import it.SimoSW.model.dao.WarehouseItemDAO;

import java.time.LocalDate;
import java.util.List;

public class DashboardController {

    private final EngineDAO engineDAO;
    private final CustomerDAO customerDAO;
    private final WarehouseItemDAO warehouseItemDAO;

    public DashboardController(EngineDAO engineDAO, CustomerDAO customerDAO, WarehouseItemDAO warehouseItemDAO) {
        this.engineDAO = engineDAO;
        this.customerDAO = customerDAO;
        this.warehouseItemDAO = warehouseItemDAO;
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

    public int getClientiServitiNelPeriodo(LocalDate from, LocalDate to) {
        return engineDAO.countDistinctCustomersDeliveredBetween(from, to);
    }

    public int getMotoriConsegnatiNelPeriodo(LocalDate from, LocalDate to) {
        return engineDAO.countDeliveredBetween(from, to);
    }

    public int getTempoMedioLavorazioneNelPeriodo(LocalDate from, LocalDate to) {
        return (int) Math.round(engineDAO.averageProcessingDaysForDeliveredBetween(from, to));
    }

    /* =========================
       Lista ultimi motori (TODO)
       ========================= */
    public List<Engine> listaUltimiMotori(int limit) {
        return engineDAO.findLatest(limit);
    }

    /* =========================
       KPI Magazzino
       ========================= */

    public int getWarehouseItemCount() {
        return warehouseItemDAO.countAll();
    }

    public int getWarehouseTotalQuantity() {
        return warehouseItemDAO.sumTotalQuantity();
    }

    public int getWarehouseOutOfStockCount() {
        return warehouseItemDAO.countOutOfStock();
    }

    public List<WarehouseItem> listaUltimiArticoliMagazzino(int limit) {
        return warehouseItemDAO.findLatest(limit);
    }
}
