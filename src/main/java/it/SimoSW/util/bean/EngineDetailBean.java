package it.SimoSW.util.bean;

import it.SimoSW.model.Engine;
import it.SimoSW.model.Image;

import java.util.List;

public class EngineDetailBean {

    private final Engine engine;
    private final List<Image> images;

    public EngineDetailBean(Engine engine, List<Image> images) {
        this.engine = engine;
        this.images = images;
    }

    public Engine getEngine() {
        return engine;
    }

    public List<Image> getImages() {
        return images;
    }
}