package it.SimoSW.model.dao;

import it.SimoSW.model.HydraulicTest;

import java.util.List;
import java.util.Optional;

public interface HydraulicTestDAO {

    HydraulicTest save(HydraulicTest hydraulicTest);

    Optional<HydraulicTest> findById(long id);

    List<HydraulicTest> findAll();

    List<HydraulicTest> search(String keyword);
}
