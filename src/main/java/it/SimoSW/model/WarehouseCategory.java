package it.SimoSW.model;

public class WarehouseCategory {
    private final Long id;
    private final String department;
    private final String name;
    private final boolean injectorFields;

    public WarehouseCategory(Long id, String department, String name, boolean injectorFields) {
        this.id = id;
        this.department = department;
        this.name = name;
        this.injectorFields = injectorFields;
    }

    public Long getId() { return id; }
    public String getDepartment() { return department; }
    public String getName() { return name; }
    public boolean isInjectorFields() { return injectorFields; }
}
