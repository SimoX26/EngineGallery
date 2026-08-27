package it.SimoSW.model;

import java.math.BigDecimal;
import java.util.Objects;

public class CatalogItem {

    private final long id;
    private final BigDecimal cylinderDiameterMm;
    private final String engineModel;
    private final int displacementCc;
    private final int valveCount;
    private final String engineCode;

    public CatalogItem(long id,
                       BigDecimal cylinderDiameterMm,
                       String engineModel,
                       int displacementCc,
                       int valveCount,
                       String engineCode) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID voce catalogo non valido");
        }
        this.id = id;
        this.cylinderDiameterMm = requirePositive(cylinderDiameterMm, "Diametro cilindro non valido");
        this.engineModel = requireNotBlank(engineModel, "Modello motore obbligatorio");
        if (displacementCc <= 0) {
            throw new IllegalArgumentException("Cilindrata non valida");
        }
        this.displacementCc = displacementCc;
        if (valveCount <= 0) {
            throw new IllegalArgumentException("Numero valvole non valido");
        }
        this.valveCount = valveCount;
        this.engineCode = requireNotBlank(engineCode, "Codice motore obbligatorio");
    }

    public long getId() {
        return id;
    }

    public BigDecimal getCylinderDiameterMm() {
        return cylinderDiameterMm;
    }

    public String getEngineModel() {
        return engineModel;
    }

    public int getDisplacementCc() {
        return displacementCc;
    }

    public int getValveCount() {
        return valveCount;
    }

    public String getEngineCode() {
        return engineCode;
    }

    private static BigDecimal requirePositive(BigDecimal value, String errorMessage) {
        Objects.requireNonNull(value, errorMessage);
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(errorMessage);
        }
        return value;
    }

    private static String requireNotBlank(String value, String errorMessage) {
        Objects.requireNonNull(value, errorMessage);
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return normalized;
    }
}
