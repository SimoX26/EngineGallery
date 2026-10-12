package it.SimoSW.model;

import java.util.Arrays;
import java.util.Optional;

public enum WarehouseComponentType {
    INJECTORS("Iniettori"),
    CONNECTING_RODS("Bielle"),
    ENGINE_BLOCK("Blocco motore"),
    CRANKSHAFT("Albero motore"),
    CAMSHAFT("Albero a camme"),
    CYLINDER_HEAD("Testata"),
    PISTONS("Pistoni"),
    VALVES("Valvole"),
    BEARINGS("Bronzine"),
    GASKETS("Guarnizioni"),
    OIL_PUMPS("Pompe olio"),
    TURBOCHARGERS("Turbocompressori"),
    OTHER("Altro"),
    UNCLASSIFIED("Non classificato");

    private final String label;

    WarehouseComponentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String getCode() {
        return name();
    }

    public static Optional<WarehouseComponentType> fromValue(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(type -> type.name().equals(value.trim()))
                .findFirst();
    }
}
