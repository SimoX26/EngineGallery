-- Macchinari interni: nessuna relazione con motori o clienti.
CREATE TABLE workshop_machines (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    model VARCHAR(120) NOT NULL,
    serial_number VARCHAR(120) NOT NULL UNIQUE,
    location VARCHAR(120) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    usage_hours INT NOT NULL DEFAULT 0
);

CREATE TABLE workshop_maintenance_plans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    machine_id BIGINT NOT NULL,
    description VARCHAR(500) NOT NULL,
    due_date DATE NULL,
    due_hours INT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_workshop_plan_machine FOREIGN KEY (machine_id) REFERENCES workshop_machines(id) ON DELETE RESTRICT,
    INDEX idx_workshop_plan_due (completed, due_date, due_hours)
);

CREATE TABLE workshop_maintenance_interventions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    machine_id BIGINT NOT NULL,
    plan_id BIGINT NULL UNIQUE,
    type VARCHAR(20) NOT NULL,
    performed_on DATE NOT NULL,
    description TEXT NOT NULL,
    parts TEXT NULL,
    cost DECIMAL(12,2) NOT NULL DEFAULT 0,
    operator_name VARCHAR(100) NOT NULL,
    CONSTRAINT fk_workshop_intervention_machine FOREIGN KEY (machine_id) REFERENCES workshop_machines(id) ON DELETE RESTRICT,
    CONSTRAINT fk_workshop_intervention_plan FOREIGN KEY (plan_id) REFERENCES workshop_maintenance_plans(id) ON DELETE RESTRICT,
    INDEX idx_workshop_intervention_date (performed_on)
);
