package it.SimoSW.model;

public class Engine {

    private long id;
    private final String engineCode;
    private EngineStatus status;

    public Engine(long id, String engineCode, EngineStatus status) {
        this.id = id;
        this.engineCode = engineCode;
        this.status = status;
    }

    public Engine(String engineCode, EngineStatus status) {
        this.engineCode = engineCode;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public String getEngineCode() {
        return engineCode;
    }

    public EngineStatus getStatus() {
        return status;
    }

    public void setStatus(EngineStatus status) {
        this.status = status;
    }
}
