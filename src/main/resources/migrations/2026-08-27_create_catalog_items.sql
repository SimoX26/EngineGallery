CREATE TABLE IF NOT EXISTS catalog_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cylinder_diameter_mm DECIMAL(6,2) NOT NULL,
    engine_model VARCHAR(120) NOT NULL,
    displacement_cc INT NOT NULL,
    valve_count INT NOT NULL,
    engine_code VARCHAR(80) NOT NULL,

    CONSTRAINT chk_catalog_items_cylinder_diameter
        CHECK (cylinder_diameter_mm > 0),
    CONSTRAINT chk_catalog_items_engine_model
        CHECK (CHAR_LENGTH(TRIM(engine_model)) > 0),
    CONSTRAINT chk_catalog_items_displacement
        CHECK (displacement_cc > 0),
    CONSTRAINT chk_catalog_items_valve_count
        CHECK (valve_count > 0),
    CONSTRAINT chk_catalog_items_engine_code
        CHECK (CHAR_LENGTH(TRIM(engine_code)) > 0)
);
