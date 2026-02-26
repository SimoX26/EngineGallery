package it.SimoSW.model;

import java.time.LocalDate;
import java.util.Objects;

public class Engine {

    /* =========================
       Identità tecnica (DB)
       ========================= */
    private long id; // assegnato dal DB

    /* =========================
       Identità di dominio
       ========================= */
    private String engineRef;   // es. RML-2026-00001
    private final String engineCode;  // es. N47D20A

    /* =========================
       Relazioni
       ========================= */
    private final long customerId;

    /* =========================
       Stato e date di dominio
       ========================= */
    private EngineStatus status;
    private final LocalDate intakeDate;
    private LocalDate deliveryDate;   // NULL finché non consegnato

    /* =========================
       Altri dati
       ========================= */
    private String notes;

    /* =========================
       Costruttore per NUOVO Engine
       ========================= */
    public Engine(
            String engineRef,
            String engineCode,
            long customerId,
            LocalDate intakeDate,
            EngineStatus status,
            String notes
    ) {
        this.engineRef = Objects.requireNonNull(engineRef);
        this.engineCode = Objects.requireNonNull(engineCode);
        this.customerId = customerId;
        this.intakeDate = Objects.requireNonNull(intakeDate);
        this.status = Objects.requireNonNull(status);
        this.notes = notes;
        this.deliveryDate = null;
    }

    /* =========================
       Costruttore per Engine persistito
       ========================= */
    public Engine(
            long id,
            String engineRef,
            String engineCode,
            long customerId,
            LocalDate intakeDate,
            EngineStatus status,
            LocalDate deliveryDate,
            String notes
    ) {
        this(engineRef, engineCode, customerId, intakeDate, status, notes);
        this.id = id;
        this.deliveryDate = deliveryDate;
    }

    /* =========================
       Getter
       ========================= */
    public long getId() {
        return id;
    }

    public String getEngineRef() {
        return engineRef;
    }

    public String getEngineCode() {
        return engineCode;
    }

    public long getCustomerId() {
        return customerId;
    }

    public EngineStatus getStatus() {
        return status;
    }

    public LocalDate getIntakeDate() {
        return intakeDate;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public String getNotes() {
        return notes;
    }

    /* =========================
       Comportamento di dominio
       ========================= */

    /**
     * Cambia lo stato del motore (uso generico).
     * Non consente di impostare DELIVERED senza data.
     */
    public void changeStatus(EngineStatus newStatus) {
        Objects.requireNonNull(newStatus);

        if (newStatus == EngineStatus.DELIVERED) {
            throw new IllegalStateException(
                    "Usa deliver(LocalDate) per consegnare un motore"
            );
        }

        this.status = newStatus;
        this.deliveryDate = null;
    }

    /**
     * Consegna il motore.
     */
    public void deliver(LocalDate deliveryDate) {
        this.status = EngineStatus.DELIVERED;
        this.deliveryDate = Objects.requireNonNull(deliveryDate);
    }

    public void updateNotes(String notes) {
        this.notes = notes;
    }

    /* =========================
       Invarianti utili
       ========================= */

    public boolean isDelivered() {
        return status == EngineStatus.DELIVERED;
    }

    public boolean isInWorkshop() {
        return status == EngineStatus.WAITING
                || status == EngineStatus.WORK_IN_PROGRESS
                || status == EngineStatus.READY;
    }

    public void assignEngineRef(String engineRef) {
        if (this.engineRef != null) {
            throw new IllegalStateException("engineRef già assegnato");
        }
        this.engineRef = Objects.requireNonNull(engineRef);
    }



    public void setId(long id) {
        this.id = id;
    }
}
