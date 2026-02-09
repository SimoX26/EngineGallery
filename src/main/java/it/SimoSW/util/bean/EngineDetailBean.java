package it.SimoSW.util.bean;

import it.SimoSW.model.Engine;
import it.SimoSW.model.Image;

import java.util.List;

public class EngineDetailBean {


    private EngineBean engine;
    private List<ImageBean> images;

    public EngineDetailBean() {}

    public EngineBean getEngine() {
        return engine;
    }

    public void setEngine(EngineBean engine) {
        this.engine = engine;
    }

    public List<ImageBean> getImages() {
        return images;
    }

    public void setImages(List<ImageBean> images) {
        this.images = images;
    }
}