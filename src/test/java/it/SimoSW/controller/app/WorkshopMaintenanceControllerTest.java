package it.SimoSW.controller.app;

import it.SimoSW.model.MaintenanceIntervention;
import it.SimoSW.model.MaintenancePlan;
import it.SimoSW.model.MaterialConsumption;
import it.SimoSW.model.WorkshopMachine;
import it.SimoSW.model.WorkshopMaterial;
import it.SimoSW.model.WorkshopMaterialUse;
import it.SimoSW.model.dao.WorkshopMaintenanceDAO;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class WorkshopMaintenanceControllerTest {
    private final StubDAO dao = new StubDAO();
    private final WorkshopMaintenanceController controller = new WorkshopMaintenanceController(dao);

    @Test
    void planRequiresExactlyOneDueCriterion() {
        assertThrows(IllegalArgumentException.class, () -> controller.createPlan(1, "Controllo", null, null));
        assertThrows(IllegalArgumentException.class, () -> controller.createPlan(1, "Controllo", LocalDate.now(), 100));
        controller.createPlan(1, "Controllo", null, 100);
        assertEquals(1, dao.savedPlans);
    }

    @Test
    void classifiesDateAndHourDeadlines() {
        assertTrue(new MaintenancePlan(1, 1, "Tornio", "Controllo", LocalDate.now().minusDays(1), null, false, 0).isOverdue());
        assertTrue(new MaintenancePlan(2, 1, "Tornio", "Controllo", LocalDate.now().plusDays(30), null, false, 0).isDueSoon());
        assertTrue(new MaintenancePlan(3, 1, "Tornio", "Controllo", null, 149, false, 100).isDueSoon());
        assertTrue(new MaintenancePlan(4, 1, "Tornio", "Controllo", null, 99, false, 100).isOverdue());
        assertFalse(new MaintenancePlan(5, 1, "Tornio", "Controllo", null, 99, true, 100).isOverdue());
    }

    @Test
    void rejectsFutureIntervention() {
        assertThrows(IllegalArgumentException.class, () -> controller.recordIntervention(1, null, "ORDINARY",
                LocalDate.now().plusDays(1), "Controllo", null, "Mario"));
    }

    @Test
    void rejectsFuturePurchaseDate() {
        assertThrows(IllegalArgumentException.class, () -> controller.createMachine("Tornio", "A", "123", "Banco",
                LocalDate.now().plusDays(1), 0));
        assertEquals(1, controller.createMachine("Tornio", "A", "123", "Banco", LocalDate.now(), 0));
    }

    @Test
    void showsOnlyMachineInterventionsFromLastYear() {
        LocalDate since = LocalDate.now().minusYears(1);
        dao.interventions = List.of(
                new MaintenanceIntervention(1, 1, "Tornio", "ORDINARY", since, "Controllo", null, "Mario"),
                new MaintenanceIntervention(2, 1, "Tornio", "ORDINARY", since.minusDays(1), "Vecchio", null, "Mario"),
                new MaintenanceIntervention(3, 2, "Fresa", "ORDINARY", LocalDate.now(), "Altro", null, "Mario"));
        assertEquals(List.of(1L), controller.getRecentInterventions(1).stream().map(MaintenanceIntervention::getId).toList());
    }

    @Test
    void validatesMaterialStockAndUnits() {
        assertThrows(IllegalArgumentException.class, () -> controller.createMaterial(
                "Filtro olio", "FILTRO", "PZ", "Scaffale A", new BigDecimal("1.5"), BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> controller.createMaterial(
                "Detergente", "DETERGENTE", "L", null, new BigDecimal("1000000000"), BigDecimal.ZERO));
        controller.createMaterial("Detergente", "DETERGENTE", "L", null,
                new BigDecimal("2.500"), new BigDecimal("0.500"));
        assertEquals(1, dao.savedMaterials);
        assertThrows(IllegalArgumentException.class, () -> controller.adjustMaterialStock(1, BigDecimal.ZERO));
    }

    @Test
    void rejectsDuplicateOrInvalidInterventionMaterials() {
        LocalDate today = LocalDate.now();
        assertThrows(IllegalArgumentException.class, () -> controller.recordIntervention(1, null, "ORDINARY", today,
                "Controllo", null, "Mario", List.of(new MaterialConsumption(1, BigDecimal.ONE),
                        new MaterialConsumption(1, BigDecimal.ONE))));
        assertThrows(IllegalArgumentException.class, () -> controller.recordIntervention(1, null, "ORDINARY", today,
                "Controllo", null, "Mario", List.of(new MaterialConsumption(1, new BigDecimal("0.0001")))));
        controller.recordIntervention(1, null, "ORDINARY", today, "Controllo", null, "Mario",
                List.of(new MaterialConsumption(1, new BigDecimal("0.250"))));
        assertEquals(new BigDecimal("0.250"), dao.savedConsumption.get(0).quantity());
    }

    private static class StubDAO implements WorkshopMaintenanceDAO {
        int savedPlans;
        int savedMaterials;
        List<MaterialConsumption> savedConsumption = List.of();
        List<MaintenanceIntervention> interventions = List.of();
        public List<WorkshopMachine> findMachines() { return List.of(new WorkshopMachine(1, "Tornio", "A", "123", "Banco", null, "ACTIVE", 100, null)); }
        public String findMachineImageFilename(long machineId) { return null; }
        public List<MaintenancePlan> findPlans() { return List.of(); }
        public List<MaintenanceIntervention> findInterventions() { return interventions; }
        public List<WorkshopMaterial> findMaterials() { return List.of(); }
        public List<WorkshopMaterialUse> findMaterialUses(long machineId) { return List.of(); }
        public long createMaterial(String name, String category, String unit, String location,
                                   BigDecimal quantity, BigDecimal minimumQuantity) { savedMaterials++; return savedMaterials; }
        public void adjustMaterialStock(long materialId, BigDecimal delta) { }
        public long createMachine(String name, String model, String serialNumber, String location, LocalDate purchaseDate, int usageHours) { return 1; }
        public boolean updateMachine(long id, String name, String model, String serialNumber, String location, LocalDate purchaseDate, String status, int usageHours) { return true; }
        public String updateMachineImage(long machineId, String imageFilename) { return null; }
        public long createPlan(long machineId, String description, LocalDate dueDate, Integer dueHours) { savedPlans++; return savedPlans; }
        public void recordIntervention(long machineId, Long planId, String type, LocalDate performedOn,
                                       String description, String parts, String operator,
                                       List<MaterialConsumption> materials) { savedConsumption = materials; }
    }
}
