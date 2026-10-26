package it.SimoSW.model.dao;

import it.SimoSW.model.MaintenanceIntervention;
import it.SimoSW.model.MaintenancePlan;
import it.SimoSW.model.MaterialConsumption;
import it.SimoSW.model.WorkshopMachine;
import it.SimoSW.model.WorkshopMaterial;
import it.SimoSW.model.WorkshopMaterialUse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface WorkshopMaintenanceDAO {
    List<WorkshopMachine> findMachines();
    String findMachineImageFilename(long machineId);
    List<MaintenancePlan> findPlans();
    List<MaintenanceIntervention> findInterventions();
    List<WorkshopMaterial> findMaterials();
    List<WorkshopMaterialUse> findMaterialUses(long machineId);
    long createMaterial(String name, String category, String unit, String location,
                        BigDecimal quantity, BigDecimal minimumQuantity);
    void adjustMaterialStock(long materialId, BigDecimal delta);
    long createMachine(String name, String model, String serialNumber, String location, LocalDate purchaseDate, int usageHours);
    boolean updateMachine(long id, String name, String model, String serialNumber, String location, LocalDate purchaseDate,
                          String status, int usageHours);
    boolean deleteMachine(long id);
    String updateMachineImage(long machineId, String imageFilename);
    long createPlan(long machineId, String description, LocalDate dueDate, Integer dueHours);
    void recordIntervention(long machineId, Long planId, String type, LocalDate performedOn,
                            String description, String parts, String operator, List<MaterialConsumption> materials);
}
