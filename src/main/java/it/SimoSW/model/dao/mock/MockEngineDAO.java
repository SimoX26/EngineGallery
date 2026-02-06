package it.SimoSW.model.dao.mock;

import it.SimoSW.model.Engine;
import it.SimoSW.model.EngineStatus;
import it.SimoSW.model.dao.EngineDAO;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class MockEngineDAO implements EngineDAO {

    private final List<Engine> engines = new ArrayList<>();
    private long nextId = 1;

    public MockEngineDAO() {

        engines.add(new Engine(nextId++, "ENG-2026-00001", "N47D20C", 1,
                LocalDate.of(2026, 1, 3), EngineStatus.WORK_IN_PROGRESS, null, "Motore BMW"));

        engines.add(new Engine(nextId++, "ENG-2026-00002", "N47D20A", 1,
                LocalDate.of(2026, 1, 4), EngineStatus.READY, null, null));

        engines.add(new Engine(nextId++, "ENG-2026-00003", "K9K", 2,
                LocalDate.of(2026, 1, 5), EngineStatus.DISASSEMBLED, null, "Renault"));

        engines.add(new Engine(nextId++, "ENG-2026-00004", "K9K", 2,
                LocalDate.of(2026, 1, 6), EngineStatus.WORK_IN_PROGRESS, null, null));

        engines.add(new Engine(nextId++, "ENG-2026-00005", "1.3 MJTD", 3,
                LocalDate.of(2026, 1, 7), EngineStatus.WORK_IN_PROGRESS, null, "Motore Fiat"));

        // DELIVERED -> deliveryDate valorizzata
        engines.add(new Engine(nextId++, "ENG-2026-00006", "M9R", 3,
                LocalDate.of(2026, 1, 2), EngineStatus.DELIVERED, LocalDate.of(2026, 1, 10), "Consegnato"));

        engines.add(new Engine(nextId++, "ENG-2026-00007", "V8-034", 4,
                LocalDate.of(2026, 1, 8), EngineStatus.WORK_IN_PROGRESS, null, "Prestazioni elevate"));

        engines.add(new Engine(nextId++, "ENG-2026-00008", "D-998", 4,
                LocalDate.of(2026, 1, 1), EngineStatus.DELIVERED, LocalDate.of(2026, 1, 9), "Storico"));
    }

    /* =====================
       CRUD
       ===================== */

    @Override
    public Engine save(Engine engine) {
        Engine saved = new Engine(
                nextId++,
                engine.getEngineRef(),
                engine.getEngineCode(),
                engine.getCustomerId(),
                engine.getIntakeDate(),
                engine.getStatus(),
                engine.getDeliveryDate(),
                engine.getNotes()
        );
        engines.add(saved);
        return saved;
    }

    @Override
    public Engine update(Engine engine) {
        for (int i = 0; i < engines.size(); i++) {
            if (engines.get(i).getId() == engine.getId()) {
                engines.set(i, engine);
                return engine;
            }
        }
        throw new NoSuchElementException("Engine non trovato: id=" + engine.getId());
    }

    /* =====================
       FIND
       ===================== */

    @Override
    public Optional<Engine> findById(long id) {
        return engines.stream()
                .filter(e -> e.getId() == id)
                .findFirst();
    }

    @Override
    public Optional<Engine> findByEngineRef(String engineRef) {
        return engines.stream()
                .filter(e -> e.getEngineRef().equals(engineRef))
                .findFirst();
    }

    @Override
    public List<Engine> findByEngineCode(String engineCode) {
        return engines.stream()
                .filter(e -> e.getEngineCode().equalsIgnoreCase(engineCode))
                .collect(Collectors.toList());
    }

    @Override
    public List<Engine> findByStatus(EngineStatus status) {
        return engines.stream()
                .filter(e -> e.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<Engine> findAll() {
        return List.copyOf(engines);
    }

    @Override
    public List<Engine> search(String keyword) {
        String k = keyword.toLowerCase();
        return engines.stream()
                .filter(e ->
                        e.getEngineRef().toLowerCase().contains(k) ||
                                e.getEngineCode().toLowerCase().contains(k) ||
                                (e.getNotes() != null && e.getNotes().toLowerCase().contains(k))
                )
                .collect(Collectors.toList());
    }

    /* =====================
       KPI
       ===================== */

    @Override
    public int countByStatus(EngineStatus status) {
        return (int) engines.stream()
                .filter(e -> e.getStatus() == status)
                .count();
    }

    @Override
    public int countInWorkshop() {
        // Decidi tu se READY è ancora "in officina":
        // io qui lo includo perché è pronto ma non consegnato.
        return (int) engines.stream()
                .filter(e ->
                        e.getStatus() == EngineStatus.WAITING ||
                                e.getStatus() == EngineStatus.DISASSEMBLED ||
                                e.getStatus() == EngineStatus.WORK_IN_PROGRESS ||
                                e.getStatus() == EngineStatus.READY
                )
                .count();
    }

    @Override
    public int countDeliveredBetween(LocalDate from, LocalDate to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);

        // inclusivo su entrambe le estremità: [from, to]
        return (int) engines.stream()
                .filter(e -> e.getStatus() == EngineStatus.DELIVERED)
                .map(Engine::getDeliveryDate)
                .filter(Objects::nonNull)
                .filter(d -> !d.isBefore(from) && !d.isAfter(to))
                .count();
    }
}