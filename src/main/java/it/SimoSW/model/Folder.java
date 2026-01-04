package it.SimoSW.model;

public class Folder {

    private long id;
    private String name;
    private Long parentId;

    public Folder() {
        // costruttore vuoto richiesto per mapping DAO
    }

    public Folder(String name, Long parentId) {
        this.name = name;
        this.parentId = parentId;
    }

    // =========================
    // Getter & Setter
    // =========================

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
