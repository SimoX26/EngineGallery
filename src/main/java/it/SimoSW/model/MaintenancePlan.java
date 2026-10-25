package it.SimoSW.model;

import java.time.LocalDate;

public class MaintenancePlan {
    private final long id, machineId;
    private final String machineName, description;
    private final LocalDate dueDate;
    private final Integer dueHours;
    private final boolean completed;
    private final int machineHours;

    public MaintenancePlan(long id, long machineId, String machineName, String description, LocalDate dueDate,
                           Integer dueHours, boolean completed, int machineHours) {
        this.id = id;
        this.machineId = machineId;
        this.machineName = machineName;
        this.description = description;
        this.dueDate = dueDate;
        this.dueHours = dueHours;
        this.completed = completed;
        this.machineHours = machineHours;
    }

    public long getId() { return id; }
    public long getMachineId() { return machineId; }
    public String getMachineName() { return machineName; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public Integer getDueHours() { return dueHours; }
    public boolean isCompleted() { return completed; }
    public int getMachineHours() { return machineHours; }
    public boolean isOverdue() { return !completed && ((dueDate != null && dueDate.isBefore(LocalDate.now()))
            || (dueHours != null && dueHours < machineHours)); }
    public boolean isDueSoon() { return !completed && !isOverdue() && ((dueDate != null && !dueDate.isAfter(LocalDate.now().plusDays(30)))
            || (dueHours != null && dueHours <= machineHours + 50)); }
}
