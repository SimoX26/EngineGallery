DROP DATABASE IF EXISTS engine_gallery;
CREATE DATABASE engine_gallery CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE engine_gallery;


CREATE USER IF NOT EXISTS 'engine_gallery'@'localhost'
IDENTIFIED BY 'engine123';


GRANT ALL PRIVILEGES ON engine_gallery.* TO 'engine_gallery'@'localhost';

FLUSH PRIVILEGES;



CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('INSPECTOR', 'OPERATOR') NOT NULL
);

CREATE TABLE customers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    surname VARCHAR(100),
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
        'DISASSEMBLED',
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






INSERT INTO users (username, password_hash, role) VALUES
('mario',     SHA2('1234', 256), 'INSPECTOR'),
('giordano',  SHA2('1234', 256), 'INSPECTOR'),
('maurizio',  SHA2('1234', 256), 'OPERATOR'),
('luigi',     SHA2('1234', 256), 'OPERATOR'),
('manuel',    SHA2('1234', 256), 'OPERATOR'),
('giggianuel',SHA2('1234', 256), 'INSPECTOR');




INSERT INTO customers (name, surname, company_name, phone, email, notes) VALUES
('Mario', 'Rossi', NULL, '3331112222', 'm.rossi@email.it', 'Cliente storico'),
('Luigi', 'Bianchi', NULL, '3334445555', 'l.bianchi@email.it', NULL),
(' ', ' ', 'Officina Auto Sprint SRL', '0811234567', 'info@autosprint.it', 'Cliente aziendale'),
('Carlo', 'Verdi', NULL, '3478889999', NULL, 'Motore arrivato molto sporco');




INSERT INTO engines
(engine_ref, engine_code, customer_id, status, intake_date, delivery_date, notes)
VALUES
-- Cliente 1: Mario Rossi
('ENG-2026-00001', 'N47D20A', 1, 'WAITING', '2026-01-03', NULL, 'Motore BMW appena arrivato'),
('ENG-2026-00002', 'N47D20A', 1, 'WORK_IN_PROGRESS', '2026-01-04', NULL, 'Catena in lavorazione'),

-- Cliente 2: Luigi Bianchi
('ENG-2026-00003', 'K9K', 2, 'DISASSEMBLED', '2026-01-05', NULL, 'Motore Renault smontato'),
('ENG-2026-00004', 'K9K', 2, 'READY', '2026-01-06', NULL, 'Pronto per riconsegna'),

-- Cliente 3: Officina Auto Sprint SRL
('ENG-2026-00005', '1.3 MJTD', 3, 'WORK_IN_PROGRESS', '2026-01-07', NULL, 'Motore Fiat'),
('ENG-2026-00006', 'M9R', 3, 'DELIVERED', '2026-01-02', '2026-01-10', 'Consegnato al cliente'),

-- Cliente 4: Carlo Verdi
('ENG-2026-00007', 'V8-034', 4, 'WORK_IN_PROGRESS', '2026-01-08', NULL, 'Motore ad alte prestazioni'),
('ENG-2026-00008', 'D-998', 4, 'DELIVERED', '2026-01-01', '2026-01-09', 'Motore storico consegnato');



INSERT INTO images (engine_id, filename, uploaded_by) VALUES
-- ENG-2026-00001
(1, 'n47_front.jpg', 3),
(1, 'n47_chain.jpg', 4),

-- ENG-2026-00002
(2, 'n47_block.jpg', 3),

-- ENG-2026-00003
(3, 'k9k_before.jpg', 4),
(3, 'k9k_open.jpg', 4),

-- ENG-2026-00004
(4, 'k9k_ready.jpg', 5),

-- ENG-2026-00006 (consegnato)
(6, 'm9r_final.jpg', 5),

-- ENG-2026-00008 (consegnato)
(8, 'd998_overview.jpg', 6),
(8, 'd998_detail.jpg', 6),

-- Upload anonimo (test uploaded_by NULL)
(1, 'n47_old_damage.jpg', NULL);