package it.SimoSW.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RecordCreationMetadataTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 8, 27, 9, 5);

    @Test
    void engineExposesFormattedCreationMetadata() {
        Engine engine = new Engine(
                1L,
                "RML-2026-00001",
                "MTR-001",
                10L,
                LocalDate.of(2026, 8, 27),
                EngineStatus.WAITING,
                null,
                null,
                CREATED_AT,
                "  simone  "
        );

        assertAll(
                () -> assertEquals("simone", engine.getCreatedBy()),
                () -> assertEquals("27 / 08 / 2026 · 09:05", engine.getCreatedAtLabel()),
                () -> assertEquals("27 / 08 / 2026", engine.getCreatedAtDateLabel()),
                () -> assertEquals("09:05", engine.getCreatedAtTimeLabel())
        );
    }

    @Test
    void warehouseItemExposesFormattedCreationMetadata() {
        WarehouseItem item = new WarehouseItem(
                2L,
                "Guarnizione",
                "SKU-2",
                3,
                "A1",
                null,
                CREATED_AT,
                "marco"
        );

        assertAll(
                () -> assertEquals("marco", item.getCreatedBy()),
                () -> assertEquals("27 / 08 / 2026 · 09:05", item.getCreatedAtLabel())
        );
    }

    @Test
    void hydraulicTestExposesFormattedCreationMetadata() {
        HydraulicTest test = new HydraulicTest(
                3L,
                "Cliente",
                "MTR-003",
                "video.mp4",
                LocalDate.of(2026, 8, 27),
                null,
                CREATED_AT,
                "larissa"
        );

        assertAll(
                () -> assertEquals("larissa", test.getCreatedBy()),
                () -> assertEquals("27 / 08 / 2026 · 09:05", test.getCreatedAtLabel())
        );
    }
}
