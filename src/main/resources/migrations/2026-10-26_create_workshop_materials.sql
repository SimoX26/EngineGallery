-- Materiali di consumo riservati alla manutenzione dei macchinari interni.
CREATE TABLE workshop_materials (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL UNIQUE,
    category VARCHAR(30) NOT NULL,
    unit VARCHAR(5) NOT NULL,
    location VARCHAR(120) NULL,
    quantity DECIMAL(12,3) NOT NULL DEFAULT 0,
    minimum_quantity DECIMAL(12,3) NOT NULL DEFAULT 0,
    CONSTRAINT chk_workshop_material_quantity CHECK (quantity >= 0),
    CONSTRAINT chk_workshop_material_minimum CHECK (minimum_quantity >= 0)
);

CREATE TABLE workshop_intervention_materials (
    intervention_id BIGINT NOT NULL,
    material_id BIGINT NOT NULL,
    material_name VARCHAR(150) NOT NULL,
    unit VARCHAR(5) NOT NULL,
    quantity DECIMAL(12,3) NOT NULL,
    PRIMARY KEY (intervention_id, material_id),
    CONSTRAINT fk_workshop_use_intervention FOREIGN KEY (intervention_id)
        REFERENCES workshop_maintenance_interventions(id) ON DELETE CASCADE,
    CONSTRAINT fk_workshop_use_material FOREIGN KEY (material_id)
        REFERENCES workshop_materials(id) ON DELETE RESTRICT,
    CONSTRAINT chk_workshop_use_quantity CHECK (quantity > 0)
);
