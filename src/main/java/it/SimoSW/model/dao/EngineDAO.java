package it.SimoSW.model.dao;

import it.SimoSW.model.Engine;
import it.SimoSW.model.EngineStatus;

import java.util.List;
import java.util.Optional;

public interface EngineDAO {

    Engine save(Engine engine);

    Engine update(Engine engine);

    Optional<Engine> findById(long id);

    Optional<Engine> findByEngineCode(String engineCode);

    List<Engine> findAll();

    List<Engine> findByStatus(EngineStatus status);

    List<Engine> findByKeyword(String keyword);

    int countMotoriInLavorazione();

    int countMotoriInOfficina();

    int countMotoriConsegnatiUltimi7Giorni();
}
