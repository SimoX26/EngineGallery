package it.SimoSW.model.dao;

import it.SimoSW.model.MaintenanceIntervention;
import it.SimoSW.model.MaintenancePlan;
import it.SimoSW.model.WorkshopMachine;
import java.time.LocalDate;
import java.util.List;

public interface WorkshopMaintenanceDAO {
    List<WorkshopMachine> findMachines();
    String findMachineImageFilename(long machineId);
    List<MaintenancePlan> findPlans();
    List<MaintenanceIntervention> findInterventions();
    long createMachine(String name, String model, String serialNumber, String location, int usageHours);
    boolean updateMachine(long id, String name, String model, String serialNumber, String location, String status, int usageHours);
    String updateMachineImage(long machineId, String imageFilename);
    long createPlan(long machineId, String description, LocalDate dueDate, Integer dueHours);
    void recordIntervention(long machineId, Long planId, String type, LocalDate performedOn,
                            String description, String parts, String operator);
}
