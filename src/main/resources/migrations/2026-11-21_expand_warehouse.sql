-- Apply once, after the existing warehouse migrations. This deliberately clears the old stock.
-- A second application fails at CREATE TABLE before reaching DELETE FROM warehouse_items.
CREATE TABLE warehouse_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    department VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    injector_fields BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE KEY uq_warehouse_category (department, name)
);

INSERT INTO warehouse_categories (department, name) VALUES
    ('Alimentazione', 'Iniettori'),
    ('Alimentazione', 'Pompe alta pressione'),
    ('Alimentazione', 'Rail'),
    ('Alimentazione', 'Tubazioni'),
    ('Alimentazione', 'Altri componenti'),
    ('Motori', 'Motori completi'),
    ('Motori', 'Testate'),
    ('Motori', 'Alberi motore'),
    ('Motori', 'Pistoni'),
    ('Motori', 'Bielle'),
    ('Motori', 'Monoblocchi'),
    ('Motori', 'Alberi a camme'),
    ('Motori', 'Valvole'),
    ('Motori', 'Bronzine'),
    ('Motori', 'Guarnizioni'),
    ('Motori', 'Pompe olio'),
    ('Altri ricambi', 'Turbocompressori'),
    ('Altri ricambi', 'Altri ricambi'),
    ('Distribuzione', 'Componenti distribuzione');

UPDATE warehouse_categories SET injector_fields = TRUE
WHERE department = 'Alimentazione' AND name = 'Iniettori';

CREATE TABLE warehouse_locations (
    code VARCHAR(120) PRIMARY KEY,
    shelf VARCHAR(40) NOT NULL,
    level_code VARCHAR(40) NOT NULL,
    container_code VARCHAR(40) NOT NULL
);

CREATE TABLE warehouse_item_details (
    item_id BIGINT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    manufacturer VARCHAR(120) NOT NULL,
    oe_code VARCHAR(120) NOT NULL,
    alternative_codes TEXT NOT NULL,
    condition_label VARCHAR(80) NOT NULL,
    lot_code VARCHAR(120) NOT NULL,
    source_engine_code VARCHAR(120) NOT NULL,
    technical_notes TEXT NOT NULL,
    manufacturer_code VARCHAR(120) NULL,
    calibration_code VARCHAR(120) NULL,
    source_cylinder VARCHAR(40) NULL,
    test_status VARCHAR(80) NULL,
    revision_status VARCHAR(80) NULL,
    CONSTRAINT fk_warehouse_details_item FOREIGN KEY (item_id) REFERENCES warehouse_items(id) ON DELETE CASCADE,
    CONSTRAINT fk_warehouse_details_category FOREIGN KEY (category_id) REFERENCES warehouse_categories(id) ON DELETE RESTRICT,
    INDEX idx_warehouse_lot (lot_code),
    INDEX idx_warehouse_source_engine (source_engine_code)
);

CREATE TABLE warehouse_engine_compatibilities (
    item_id BIGINT NOT NULL,
    engine_code VARCHAR(120) NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (item_id, engine_code),
    INDEX idx_warehouse_compat_engine (engine_code),
    CONSTRAINT fk_warehouse_compat_item FOREIGN KEY (item_id) REFERENCES warehouse_items(id) ON DELETE CASCADE
);

-- Child rows in warehouse_images, warehouse_item_details and
-- warehouse_engine_compatibilities are removed by their cascading FKs.
-- With the webapp stopped, run 2026-11-21_purge_warehouse_images.sh on the
-- active warehouse upload directory after this migration to remove old files.
DELETE FROM warehouse_items;

ALTER TABLE warehouse_items
    MODIFY COLUMN sku VARCHAR(80) NOT NULL,
    MODIFY COLUMN location VARCHAR(120) NOT NULL;
