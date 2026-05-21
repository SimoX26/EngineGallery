package it.SimoSW.controller.app;

import it.SimoSW.model.HydraulicTest;
import it.SimoSW.model.dao.HydraulicTestDAO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class HydraulicTestController {

    private final HydraulicTestDAO hydraulicTestDAO;

    public HydraulicTestController(HydraulicTestDAO hydraulicTestDAO) {
        this.hydraulicTestDAO = hydraulicTestDAO;
    }

    public List<HydraulicTest> getAllHydraulicTests() {
        return hydraulicTestDAO.findAll();
    }

    public int countHydraulicTestsBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            return 0;
        }
        return hydraulicTestDAO.countByTestDateBetween(from, to);
    }

    public List<HydraulicTest> searchHydraulicTests(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return hydraulicTestDAO.search(keyword.trim());
    }

    public HydraulicTest createHydraulicTest(String customerName,
                                             String engineCode,
                                             String videoUrl,
                                             String testDate,
                                             String notes) {
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("Nome cliente obbligatorio");
        }
        if (engineCode == null || engineCode.isBlank()) {
            throw new IllegalArgumentException("Codice motore obbligatorio");
        }
        if (videoUrl == null || videoUrl.isBlank()) {
            throw new IllegalArgumentException("URL video obbligatorio");
        }
        if (testDate == null || testDate.isBlank()) {
            throw new IllegalArgumentException("Data prova obbligatoria");
        }

        HydraulicTest hydraulicTest = new HydraulicTest(
                customerName.trim(),
                engineCode.trim(),
                videoUrl.trim(),
                LocalDate.parse(testDate.trim()),
                normalizeOptional(notes)
        );

        return hydraulicTestDAO.save(hydraulicTest);
    }

    public Optional<HydraulicTest> findHydraulicTestById(long id) {
        if (id <= 0) {
            return Optional.empty();
        }
        return hydraulicTestDAO.findById(id);
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
