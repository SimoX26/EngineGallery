package it.SimoSW.model;

import java.time.LocalDate;

public class MaintenanceIntervention {
    private final long id, machineId;
    private final String machineName, type, description, parts, operator;
    private final LocalDate performedOn;

    public MaintenanceIntervention(long id, long machineId, String machineName, String type, LocalDate performedOn,
                                   String description, String parts, String operator) {
        this.id = id;
        this.machineId = machineId;
        this.machineName = machineName;
        this.type = type;
        this.performedOn = performedOn;
        this.description = description;
        this.parts = parts;
        this.operator = operator;
    }

    public long getId() { return id; }
    public long getMachineId() { return machineId; }
    public String getMachineName() { return machineName; }
    public String getType() { return type; }
    public LocalDate getPerformedOn() { return performedOn; }
    public String getDescription() { return description; }
    public String getParts() { return parts; }
    public String getOperator() { return operator; }
}
