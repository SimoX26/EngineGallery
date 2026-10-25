package it.SimoSW.controller.app;

import it.SimoSW.model.MaintenanceIntervention;
import it.SimoSW.model.MaintenancePlan;
import it.SimoSW.model.WorkshopMachine;
import it.SimoSW.model.dao.WorkshopMaintenanceDAO;
import java.time.LocalDate;
import java.util.List;

public class WorkshopMaintenanceController {
    private final WorkshopMaintenanceDAO dao;

    public WorkshopMaintenanceController(WorkshopMaintenanceDAO dao) { this.dao = dao; }
    public List<WorkshopMachine> getMachines() { return dao.findMachines(); }
    public String getMachineImageFilename(long machineId) { return dao.findMachineImageFilename(machineId); }
    public List<MaintenancePlan> getPlans() { return dao.findPlans(); }
    public List<MaintenanceIntervention> getInterventions() { return dao.findInterventions(); }

    public String updateMachineImage(long machineId, String imageFilename) {
        requireMachine(machineId);
        return dao.updateMachineImage(machineId, required(imageFilename, 255, "Immagine"));
    }

    public long createMachine(String name, String model, String serial, String location, int hours) {
        validateHours(hours);
        return dao.createMachine(required(name, 120, "Nome"), required(model, 120, "Modello"),
                required(serial, 120, "Matricola"), required(location, 120, "Posizione"), hours);
    }

    public void updateMachine(long id, String name, String model, String serial, String location, String status, int hours) {
        validateHours(hours);
        if (!"ACTIVE".equals(status) && !"OUT_OF_SERVICE".equals(status))
            throw new IllegalArgumentException("Stato non valido");
        if (!dao.updateMachine(id, required(name, 120, "Nome"), required(model, 120, "Modello"),
                required(serial, 120, "Matricola"), required(location, 120, "Posizione"), status, hours))
            throw new IllegalArgumentException("Macchinario non trovato");
    }

    public void createPlan(long machineId, String description, LocalDate dueDate, Integer dueHours) {
        requireMachine(machineId);
        if ((dueDate == null) == (dueHours == null))
            throw new IllegalArgumentException("Indica una scadenza per data oppure per ore");
        if (dueHours != null && dueHours < 0) throw new IllegalArgumentException("Ore di scadenza non valide");
        dao.createPlan(machineId, required(description, 500, "Descrizione"), dueDate, dueHours);
    }

    public void recordIntervention(long machineId, Long planId, String type, LocalDate date,
                                   String description, String parts, String operator) {
        requireMachine(machineId);
        if (!"ORDINARY".equals(type) && !"EXTRAORDINARY".equals(type))
            throw new IllegalArgumentException("Tipo di intervento non valido");
        if (date == null || date.isAfter(LocalDate.now())) throw new IllegalArgumentException("Data intervento non valida");
        dao.recordIntervention(machineId, planId, type, date, required(description, 5000, "Descrizione"),
                optional(parts, 5000, "Ricambi"), required(operator, 100, "Operatore"));
    }

    private void requireMachine(long id) {
        if (id <= 0 || dao.findMachines().stream().noneMatch(machine -> machine.getId() == id))
            throw new IllegalArgumentException("Macchinario non trovato");
    }

    private static void validateHours(int hours) {
        if (hours < 0) throw new IllegalArgumentException("Ore di utilizzo non valide");
    }

    private static String required(String value, int max, String label) {
        String result = optional(value, max, label);
        if (result == null) throw new IllegalArgumentException(label + " obbligatorio");
        return result;
    }

    private static String optional(String value, int max, String label) {
        String result = value == null ? "" : value.trim();
        if (result.length() > max) throw new IllegalArgumentException(label + " troppo lungo");
        return result.isEmpty() ? null : result;
    }
}
