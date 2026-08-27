package it.SimoSW.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Engine {

    private static final DateTimeFormatter CREATED_AT_FORMATTER =
            DateTimeFormatter.ofPattern("dd / MM / yyyy · HH:mm");
    private static final DateTimeFormatter CREATED_AT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd / MM / yyyy");
    private static final DateTimeFormatter CREATED_AT_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

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
    private LocalDateTime createdAt;
    private String createdBy;

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
        this(engineRef, engineCode, customerId, intakeDate, status, notes, null);
    }

    public Engine(
            String engineRef,
            String engineCode,
            long customerId,
            LocalDate intakeDate,
            EngineStatus status,
            String notes,
            String createdBy
    ) {
        this.engineRef = Objects.requireNonNull(engineRef);
        this.engineCode = Objects.requireNonNull(engineCode);
        this.customerId = customerId;
        this.intakeDate = Objects.requireNonNull(intakeDate);
        this.status = Objects.requireNonNull(status);
        this.notes = notes;
        this.deliveryDate = null;
        this.createdAt = null;
        this.createdBy = normalizeOptional(createdBy);
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
        this(id, engineRef, engineCode, customerId, intakeDate, status, deliveryDate, notes, null, null);
    }

    public Engine(
            long id,
            String engineRef,
            String engineCode,
            long customerId,
            LocalDate intakeDate,
            EngineStatus status,
            LocalDate deliveryDate,
            String notes,
            LocalDateTime createdAt,
            String createdBy
    ) {
        this(engineRef, engineCode, customerId, intakeDate, status, notes, createdBy);
        this.id = id;
        this.deliveryDate = deliveryDate;
        this.createdAt = createdAt;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getCreatedAtLabel() {
        return createdAt != null ? CREATED_AT_FORMATTER.format(createdAt) : "";
    }

    public String getCreatedAtDateLabel() {
        return createdAt != null ? CREATED_AT_DATE_FORMATTER.format(createdAt) : "";
    }

    public String getCreatedAtTimeLabel() {
        return createdAt != null ? CREATED_AT_TIME_FORMATTER.format(createdAt) : "";
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

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
