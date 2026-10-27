-- Bootstrap schema for new installations only.
-- Do not use this file to update an existing installation with data already present.
-- For existing installations, keep the current schema/data and apply only the
-- required SQL migrations plus secure provisioning of the MySQL application user.
CREATE DATABASE IF NOT EXISTS engine_gallery CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE engine_gallery;

-- Provisioning of the database user is intentionally external to this script.
-- Run this bootstrap using a MySQL account that already has permission to
-- create the schema and tables, then configure the application with:
--   -Denginegallery.db.url / ENGINE_GALLERY_DB_URL
--   -Denginegallery.db.user / ENGINE_GALLERY_DB_USER
--   -Denginegallery.db.password / ENGINE_GALLERY_DB_PASSWORD



CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'OPERATOR') NOT NULL
);

CREATE TABLE android_push_tokens (
    token VARCHAR(512) CHARACTER SET ascii COLLATE ascii_bin PRIMARY KEY,
    user_id BIGINT NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_android_push_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);


CREATE TABLE customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    company_name VARCHAR(150),
    phone VARCHAR(30),
    email VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);



CREATE TABLE engines (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    engine_ref VARCHAR(30) NOT NULL UNIQUE,
    engine_code VARCHAR(50) NOT NULL,

    customer_id BIGINT NOT NULL,

    status ENUM(
        'WAITING',
        'WORK_IN_PROGRESS',
        'READY',
        'DELIVERED'
    ) NOT NULL,

    intake_date DATE NOT NULL,
    delivery_date DATE DEFAULT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),

    CONSTRAINT fk_engine_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id)
        ON DELETE RESTRICT
);



CREATE TABLE hydraulic_tests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_name VARCHAR(100) NOT NULL,
    engine_code VARCHAR(80) NOT NULL,
    video_url VARCHAR(500) NOT NULL,
    test_date DATE NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

CREATE TABLE images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    filename VARCHAR(255) NOT NULL,
    engine_id BIGINT NOT NULL,
    uploaded_by BIGINT,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_image_engine
        FOREIGN KEY (engine_id)
        REFERENCES engines(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_image_user
        FOREIGN KEY (uploaded_by)
        REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE TABLE warehouse_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    component_type VARCHAR(50) NOT NULL DEFAULT 'UNCLASSIFIED',
    name VARCHAR(120) NOT NULL,
    sku VARCHAR(80),
    quantity INT NOT NULL DEFAULT 0,
    location VARCHAR(120),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

CREATE TABLE ready_delivery_engines (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    engine_code VARCHAR(80) NOT NULL UNIQUE,
    model VARCHAR(120) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    notes TEXT NULL,
    image_filename VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    CONSTRAINT chk_ready_delivery_quantity CHECK (quantity >= 0)
);

CREATE TABLE catalog_items (
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

CREATE TABLE vehicles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plate VARCHAR(20) NOT NULL UNIQUE,
    make VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    notes TEXT NULL
);

CREATE TABLE vehicle_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vehicle_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    title VARCHAR(150) NOT NULL,
    due_date DATE NOT NULL,
    completed_on DATE NULL,
    notes TEXT NULL,
    CONSTRAINT fk_vehicle_event_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE,
    INDEX idx_vehicle_event_due (completed_on, due_date)
);

CREATE TABLE workshop_machines (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    model VARCHAR(120) NOT NULL,
    serial_number VARCHAR(120) NOT NULL UNIQUE,
    location VARCHAR(120) NOT NULL,
    purchase_date DATE NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    usage_hours INT NOT NULL DEFAULT 0,
    image_filename VARCHAR(255) NULL
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

CREATE TABLE warehouse_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warehouse_item_id BIGINT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    uploaded_by BIGINT,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_warehouse_image_item
        FOREIGN KEY (warehouse_item_id)
        REFERENCES warehouse_items(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_warehouse_image_user
        FOREIGN KEY (uploaded_by)
        REFERENCES users(id)
        ON DELETE SET NULL
);






INSERT INTO users (username, password_hash, role) VALUES
('mario',     SHA2('rml', 256), 'OPERATOR'),
('giordano',  SHA2('rml', 256), 'OPERATOR'),
('giuliano',  SHA2('rml', 256), 'OPERATOR'),
('maurizio',  SHA2('rml', 256), 'OPERATOR'),
('luigi',     SHA2('rml', 256), 'OPERATOR'),
('giggi',     SHA2('rml', 256), 'OPERATOR'),
('manuel',    SHA2('rml', 256), 'OPERATOR'),
('simone',    SHA2('rml', 256), 'OPERATOR'),
('emanuele',  SHA2('rml', 256), 'OPERATOR'),
('capo',      SHA2('rml', 256), 'ADMIN'),
('federica',  SHA2('rml', 256), 'OPERATOR'),
('Marco',     SHA2('rml', 256), 'OPERATOR'),
('Larissa',   SHA2('rml', 256), 'OPERATOR');
