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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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
    name VARCHAR(120) NOT NULL,
    sku VARCHAR(80),
    quantity INT NOT NULL DEFAULT 0,
    location VARCHAR(120),
    notes TEXT
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
