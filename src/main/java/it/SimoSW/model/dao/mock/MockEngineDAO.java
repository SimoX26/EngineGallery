package it.SimoSW.model.dao.mock;

import it.SimoSW.model.Engine;
import it.SimoSW.model.EngineStatus;
import it.SimoSW.model.dao.EngineDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MockEngineDAO implements EngineDAO {

    private final List<Engine> engines = new ArrayList<>();

    public MockEngineDAO() {
        engines.add(new Engine(1, "N47D20C", EngineStatus.WORK_IN_PROGRESS));
        engines.add(new Engine(2, "N47D20A", EngineStatus.READY));
        engines.add(new Engine(3, "K9K", EngineStatus.WAITING));
        engines.add(new Engine(4, "K9K", EngineStatus.DISASSEMBLED));
        engines.add(new Engine(5, "1.3 MJTD", EngineStatus.DELIVERED));
        engines.add(new Engine(6, "CURSOR", EngineStatus.DELIVERED));
        engines.add(new Engine(7, "FIRE", EngineStatus.READY));
        engines.add(new Engine(8, "VW", EngineStatus.WORK_IN_PROGRESS));
        engines.add(new Engine(9, "M9R", EngineStatus.DELIVERED));

    }

    @Override
    public Engine save(Engine engine) {
        return null;
    }

    @Override
    public Engine update(Engine engine) {
        return null;
    }

    @Override
    public Optional<Engine> findById(long id) {
        return Optional.empty();
    }

    @Override
    public Optional<Engine> findByEngineCode(String engineCode) {
        return Optional.empty();
    }

    @Override
    public List<Engine> findAll() {
        return List.copyOf(engines);
    }


    @Override
    public List<Engine> findByStatus(EngineStatus status) {
        return List.of();
    }

    @Override
    public List<Engine> findByKeyword(String keyword) {
        return List.of();
    }

    @Override
    public int countWorkInProgressEngines() {
        return (int) engines.stream()
                .filter(e -> e.getStatus() == EngineStatus.WORK_IN_PROGRESS)
                .count();
    }

    @Override
    public int countMotoriInOfficina() {
        return engines.size();
    }

    @Override
    public int countMotoriConsegnatiUltimi7Giorni() {
        return (int) engines.stream()
                .filter(e -> e.getStatus() == EngineStatus.DELIVERED)
                .count();
    }

}
