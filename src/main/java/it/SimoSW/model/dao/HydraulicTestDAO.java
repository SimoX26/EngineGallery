package it.SimoSW.model.dao;

import it.SimoSW.model.HydraulicTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HydraulicTestDAO {

    HydraulicTest save(HydraulicTest hydraulicTest);

    void update(HydraulicTest hydraulicTest);

    boolean delete(long id);

    Optional<HydraulicTest> findById(long id);

    List<HydraulicTest> findAll();

    int countByTestDateBetween(LocalDate from, LocalDate to);

    List<HydraulicTest> search(String keyword);
}
