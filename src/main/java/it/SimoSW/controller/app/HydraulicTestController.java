package it.SimoSW.controller.app;

import it.SimoSW.model.HydraulicTest;
import it.SimoSW.model.dao.HydraulicTestDAO;

import java.util.List;

public class HydraulicTestController {

    private final HydraulicTestDAO hydraulicTestDAO;

    public HydraulicTestController(HydraulicTestDAO hydraulicTestDAO) {
        this.hydraulicTestDAO = hydraulicTestDAO;
    }

    public List<HydraulicTest> getAllHydraulicTests() {
        return hydraulicTestDAO.findAll();
    }

    public List<HydraulicTest> searchHydraulicTests(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return hydraulicTestDAO.search(keyword.trim());
    }
}
