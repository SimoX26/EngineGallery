package it.SimoSW.controller.app;

import it.SimoSW.model.MaintenanceIntervention;
import it.SimoSW.model.MaintenancePlan;
import it.SimoSW.model.MaterialConsumption;
import it.SimoSW.model.WorkshopMachine;
import it.SimoSW.model.WorkshopMaterial;
import it.SimoSW.model.WorkshopMaterialUse;
import it.SimoSW.model.dao.WorkshopMaintenanceDAO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorkshopMaintenanceController {
    private final WorkshopMaintenanceDAO dao;

    public WorkshopMaintenanceController(WorkshopMaintenanceDAO dao) { this.dao = dao; }
    public List<WorkshopMachine> getMachines() { return dao.findMachines(); }
    public String getMachineImageFilename(long machineId) { return dao.findMachineImageFilename(machineId); }
    public List<MaintenancePlan> getPlans() { return dao.findPlans(); }
    public List<MaintenanceIntervention> getInterventions() { return dao.findInterventions(); }
    public List<WorkshopMaterial> getMaterials() { return dao.findMaterials(); }
    public List<WorkshopMaterialUse> getMaterialUses(long machineId) { return dao.findMaterialUses(machineId); }

    public long createMaterial(String name, String category, String unit, String location,
                               BigDecimal quantity, BigDecimal minimumQuantity) {
        if (category == null || !Set.of("FILTRO", "DETERGENTE", "LUBRIFICANTE", "LIQUIDO", "RICAMBIO", "ALTRO").contains(category))
            throw new IllegalArgumentException("Categoria materiale non valida");
        if (unit == null || !Set.of("PZ", "L", "KG", "ML").contains(unit))
            throw new IllegalArgumentException("Unità di misura non valida");
        BigDecimal initial = nonNegativeQuantity(quantity, "Giacenza iniziale");
        BigDecimal minimum = nonNegativeQuantity(minimumQuantity, "Scorta minima");
        if ("PZ".equals(unit) && (initial.stripTrailingZeros().scale() > 0 || minimum.stripTrailingZeros().scale() > 0))
            throw new IllegalArgumentException("I pezzi devono avere quantità intere");
        return dao.createMaterial(required(name, 150, "Nome materiale"), category, unit,
                optional(location, 120, "Ubicazione"), initial, minimum);
    }

    public void adjustMaterialStock(long materialId, BigDecimal delta) {
        if (materialId <= 0) throw new IllegalArgumentException("Materiale non valido");
        if (delta == null || delta.signum() == 0 || quantityOutOfRange(delta))
            throw new IllegalArgumentException("Quantità di rettifica non valida");
        dao.adjustMaterialStock(materialId, delta);
    }
    public List<MaintenanceIntervention> getRecentInterventions(long machineId) {
        LocalDate since = LocalDate.now().minusYears(1);
        return dao.findInterventions().stream()
                .filter(item -> item.getMachineId() == machineId && !item.getPerformedOn().isBefore(since)).toList();
    }

    public String updateMachineImage(long machineId, String imageFilename) {
        requireMachine(machineId);
        return dao.updateMachineImage(machineId, required(imageFilename, 255, "Immagine"));
    }

    public long createMachine(String name, String model, String serial, String location, LocalDate purchaseDate, int hours) {
        validateHours(hours);
        validatePurchaseDate(purchaseDate);
        return dao.createMachine(required(name, 120, "Nome"), required(model, 120, "Modello"),
                required(serial, 120, "Matricola"), required(location, 120, "Posizione"), purchaseDate, hours);
    }

    public void updateMachine(long id, String name, String model, String serial, String location, LocalDate purchaseDate,
                              String status, int hours) {
        validateHours(hours);
        validatePurchaseDate(purchaseDate);
        if (!"ACTIVE".equals(status) && !"OUT_OF_SERVICE".equals(status))
            throw new IllegalArgumentException("Stato non valido");
        if (!dao.updateMachine(id, required(name, 120, "Nome"), required(model, 120, "Modello"),
                required(serial, 120, "Matricola"), required(location, 120, "Posizione"), purchaseDate, status, hours))
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
        recordIntervention(machineId, planId, type, date, description, parts, operator, List.of());
    }

    public void recordIntervention(long machineId, Long planId, String type, LocalDate date,
                                   String description, String parts, String operator,
                                   List<MaterialConsumption> materials) {
        requireMachine(machineId);
        if (!"ORDINARY".equals(type) && !"EXTRAORDINARY".equals(type))
            throw new IllegalArgumentException("Tipo di intervento non valido");
        if (date == null || date.isAfter(LocalDate.now())) throw new IllegalArgumentException("Data intervento non valida");
        if (materials == null || materials.size() > 30) throw new IllegalArgumentException("Troppi materiali selezionati");
        Set<Long> selected = new HashSet<>();
        for (MaterialConsumption item : materials) {
            if (item == null || item.materialId() <= 0 || !selected.add(item.materialId()))
                throw new IllegalArgumentException("Materiale duplicato o non valido");
            positiveQuantity(item.quantity(), "Quantità prelevata");
        }
        dao.recordIntervention(machineId, planId, type, date, required(description, 5000, "Descrizione"),
                optional(parts, 5000, "Ricambi"), required(operator, 100, "Operatore"), materials);
    }

    private void requireMachine(long id) {
        if (id <= 0 || dao.findMachines().stream().noneMatch(machine -> machine.getId() == id))
            throw new IllegalArgumentException("Macchinario non trovato");
    }

    private static void validateHours(int hours) {
        if (hours < 0) throw new IllegalArgumentException("Ore di utilizzo non valide");
    }

    private static void validatePurchaseDate(LocalDate purchaseDate) {
        if (purchaseDate != null && purchaseDate.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Data di acquisto non valida");
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

    private static BigDecimal nonNegativeQuantity(BigDecimal value, String label) {
        if (value == null || value.signum() < 0 || quantityOutOfRange(value))
            throw new IllegalArgumentException(label + " non valida");
        return value;
    }

    private static BigDecimal positiveQuantity(BigDecimal value, String label) {
        if (value == null || value.signum() <= 0 || quantityOutOfRange(value))
            throw new IllegalArgumentException(label + " non valida");
        return value;
    }

    private static boolean quantityOutOfRange(BigDecimal value) {
        BigDecimal normalized = value.abs().stripTrailingZeros();
        return normalized.scale() > 3 || normalized.precision() - normalized.scale() > 9;
    }
}
