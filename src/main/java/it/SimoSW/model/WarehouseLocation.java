package it.SimoSW.model;

public class WarehouseLocation {
    private final String shelf;
    private final String level;
    private final String container;

    public WarehouseLocation(String shelf, String level, String container) {
        this.shelf = shelf;
        this.level = level;
        this.container = container;
    }

    public String getShelf() { return shelf; }
    public String getLevel() { return level; }
    public String getContainer() { return container; }
    public String getCode() { return shelf + "-" + level + "-" + container; }
}
