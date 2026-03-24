package it.SimoSW.model.dao;

import it.SimoSW.model.HydraulicTest;

import java.util.List;

public interface HydraulicTestDAO {

    List<HydraulicTest> findAll();

    List<HydraulicTest> search(String keyword);
}
