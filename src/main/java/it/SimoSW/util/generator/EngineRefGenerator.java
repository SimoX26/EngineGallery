package it.SimoSW.util.generator;


import it.SimoSW.model.dao.EngineDAO;

import java.time.LocalDate;

public class EngineRefGenerator {

    private final EngineDAO engineDAO;

    public EngineRefGenerator(EngineDAO engineDAO) {
        this.engineDAO = engineDAO;
    }

    public String generate() {
        int year = LocalDate.now().getYear();
        int nextSeq = engineDAO.getNextSequenceForYear(year);
        return String.format("ENG-%d-%05d", year, nextSeq);
    }
}