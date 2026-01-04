package it.SimoSW.model;

import java.util.List;

public class Image {

    private long id;
    private long engineId;
    private String filePath;

    private String description;
    private List<String> keywords;

    public Image(long id, long engineId, String filePath, String description, List<String> keywords) {
        this.id = id;
        this.engineId = engineId;
        this.filePath = filePath;
        this.description = description;
        this.keywords = keywords;
    }

    public Image(long engineId, String filePath, String description, List<String> keywords) {
        this.engineId = engineId;
        this.filePath = filePath;
        this.description = description;
        this.keywords = keywords;
    }

    public long getId() {
        return id;
    }

    public long getEngineId() {
        return engineId;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}
