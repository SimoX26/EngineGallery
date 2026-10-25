package it.SimoSW.controller.app;

import it.SimoSW.model.MaintenanceIntervention;
import it.SimoSW.model.MaintenancePlan;
import it.SimoSW.model.WorkshopMachine;
import it.SimoSW.model.dao.WorkshopMaintenanceDAO;
import org.junit.jupiter.api.Test;
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

    private static class StubDAO implements WorkshopMaintenanceDAO {
        int savedPlans;
        public List<WorkshopMachine> findMachines() { return List.of(new WorkshopMachine(1, "Tornio", "A", "123", "Banco", "ACTIVE", 100, null)); }
        public String findMachineImageFilename(long machineId) { return null; }
        public List<MaintenancePlan> findPlans() { return List.of(); }
        public List<MaintenanceIntervention> findInterventions() { return List.of(); }
        public long createMachine(String name, String model, String serialNumber, String location, int usageHours) { return 1; }
        public boolean updateMachine(long id, String name, String model, String serialNumber, String location, String status, int usageHours) { return true; }
        public String updateMachineImage(long machineId, String imageFilename) { return null; }
        public long createPlan(long machineId, String description, LocalDate dueDate, Integer dueHours) { savedPlans++; return savedPlans; }
        public void recordIntervention(long machineId, Long planId, String type, LocalDate performedOn,
                                       String description, String parts, String operator) { }
    }
}
