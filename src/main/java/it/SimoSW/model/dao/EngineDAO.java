package it.SimoSW.model.dao;

import it.SimoSW.model.Engine;
import it.SimoSW.model.EngineStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EngineDAO {

    /* =========================
       Persistenza
       ========================= */
    Engine save(Engine engine);

    Engine update(Engine engine);

    /* =========================
       Recupero per identità
       ========================= */
    Optional<Engine> findById(long id);                 // tecnico
    Optional<Engine> findByEngineRef(String engineRef); // dominio

    /* =========================
       Recupero per filtri
       ========================= */
    List<Engine> findByEngineCode(String engineCode);   // NON più Optional
    List<Engine> findByStatus(EngineStatus status);
    List<Engine> findAll();

    /* =========================
       Ricerca testuale
       ========================= */
    List<Engine> search(String keyword);

    /* =========================
       Dashboard / KPI
       ========================= */
    int countByStatus(EngineStatus status);
    int countInWorkshop();
    int countDeliveredBetween(LocalDate from, LocalDate to);
}