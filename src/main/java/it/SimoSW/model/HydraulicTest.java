package it.SimoSW.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class HydraulicTest {

    private static final DateTimeFormatter CREATED_AT_FORMATTER =
            DateTimeFormatter.ofPattern("dd / MM / yyyy · HH:mm");

    private final long id;
    private final String customerName;
    private final String engineCode;
    private final String videoUrl;
    private final LocalDate testDate;
    private final String notes;
    private final LocalDateTime createdAt;
    private final String createdBy;

    public HydraulicTest(String customerName,
                         String engineCode,
                         String videoUrl,
                         LocalDate testDate,
                         String notes) {
        this(0L, customerName, engineCode, videoUrl, testDate, notes, null, null);
    }

    public HydraulicTest(String customerName,
                         String engineCode,
                         String videoUrl,
                         LocalDate testDate,
                         String notes,
                         String createdBy) {
        this(0L, customerName, engineCode, videoUrl, testDate, notes, null, createdBy);
    }

    public HydraulicTest(long id,
                         String customerName,
                         String engineCode,
                         String videoUrl,
                         LocalDate testDate,
                         String notes,
                         LocalDateTime createdAt) {
        this(id, customerName, engineCode, videoUrl, testDate, notes, createdAt, null);
    }

    public HydraulicTest(long id,
                         String customerName,
                         String engineCode,
                         String videoUrl,
                         LocalDate testDate,
                         String notes,
                         LocalDateTime createdAt,
                         String createdBy) {
        this.id = id;
        this.customerName = Objects.requireNonNull(customerName, "Nome cliente obbligatorio");
        this.engineCode = Objects.requireNonNull(engineCode, "Codice motore obbligatorio");
        this.videoUrl = Objects.requireNonNull(videoUrl, "URL video obbligatorio");
        this.testDate = Objects.requireNonNull(testDate, "Data prova obbligatoria");
        this.notes = notes;
        this.createdAt = createdAt;
        this.createdBy = normalizeOptional(createdBy);
    }

    public long getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getEngineCode() {
        return engineCode;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public LocalDate getTestDate() {
        return testDate;
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

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
