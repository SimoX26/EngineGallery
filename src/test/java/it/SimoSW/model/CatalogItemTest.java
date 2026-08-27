package it.SimoSW.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CatalogItemTest {

    @Test
    void exposesValidCatalogData() {
        CatalogItem item = item(1L, new BigDecimal("86.50"), "FIAT", 1998, 16, "TEST-001");

        assertAll(
                () -> assertEquals(1L, item.getId()),
                () -> assertEquals(new BigDecimal("86.50"), item.getCylinderDiameterMm()),
                () -> assertEquals("FIAT", item.getEngineModel()),
                () -> assertEquals(1998, item.getDisplacementCc()),
                () -> assertEquals(16, item.getValveCount()),
                () -> assertEquals("TEST-001", item.getEngineCode())
        );
    }

    @Test
    void rejectsNonPositiveNumericValues() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> item(0L, new BigDecimal("86.50"), "FIAT", 1998, 16, "TEST-001")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> item(1L, BigDecimal.ZERO, "FIAT", 1998, 16, "TEST-001")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> item(1L, new BigDecimal("86.50"), "FIAT", 0, 16, "TEST-001")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> item(1L, new BigDecimal("86.50"), "FIAT", 1998, 0, "TEST-001"))
        );
    }

    @Test
    void rejectsBlankRequiredTextValues() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> item(1L, new BigDecimal("86.50"), "   ", 1998, 16, "TEST-001")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> item(1L, new BigDecimal("86.50"), "FIAT", 1998, 16, ""))
        );
    }

    private static CatalogItem item(long id,
                                    BigDecimal cylinderDiameterMm,
                                    String engineModel,
                                    int displacementCc,
                                    int valveCount,
                                    String engineCode) {
        return new CatalogItem(
                id,
                cylinderDiameterMm,
                engineModel,
                displacementCc,
                valveCount,
                engineCode
        );
    }
}
