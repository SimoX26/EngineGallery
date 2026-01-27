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
    engine_code VARCHAR(50) UNIQUE NOT NULL,
    customer_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    intake_date DATE NOT NULL,
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



INSERT INTO engines (engine_code, customer_id, status, intake_date, notes) VALUES
('V8-034',   1, 'INCOMING',     '2026-01-05', 'Motore completo'),
('V8-035',   3, 'DISASSEMBLED', '2026-01-06', 'Smontaggio in corso'),
('V6-112',   2, 'IN_PROGRESS',  '2026-01-08', NULL),
('K9K',      3, 'IN_PROGRESS',  '2026-01-08', 'Motore Renault'),
('N47D20C',  2, 'IN_PROGRESS',  '2026-01-08', 'Motore BMW'),
('1.3 MJTD', 1, 'IN_PROGRESS',  '2026-01-08', 'Motore Fiat'),
('M9R',      1, 'IN_PROGRESS',  '2026-01-08', NULL),
('D-998',    4, 'COMPLETED',    '2026-01-02', 'Pronto per riconsegna');



INSERT INTO images (engine_id, filename, uploaded_by) VALUES
-- Motore V8-034
(1, 'v8_034_front.jpg', 3),
(1, 'v8_034_side.jpg',  4),

-- Motore V8-035
(2, 'v8_035_open.jpg',  5),
(2, 'v8_035_block.jpg', 3),

-- Motore V6-112
(3, 'v6_112_dirty.jpg', 4),

-- Motore K9K
(4, 'k9k_before.jpg',   5),
(4, 'k9k_after.jpg',    5),

-- Motore N47D20C
(5, 'n47_chain.jpg',    3),

-- Motore D-998 (completato)
(8, 'd998_final.jpg',   6),

-- Upload anonimo (test uploaded_by NULL)
(1, 'v8_034_old.jpg',   NULL);
