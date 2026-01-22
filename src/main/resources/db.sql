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
('V8-034', 1, 'INCOMING',      '2026-01-05', 'Motore completo'),
('V8-035', 3, 'DISASSEMBLED',  '2026-01-06', 'Smontaggio in corso'),
('V6-112', 2, 'IN_PROGRESS',   '2026-01-08', NULL),
('k9k', 3, 'IN_PROGRESS',   '2026-01-08', NULL),
('N47D20C', 2, 'IN_PROGRESS',   '2026-01-08', NULL),
('1.3 MJTD', 1, 'IN_PROGRESS',   '2026-01-08', NULL),
('M9R', 1, 'IN_PROGRESS',   '2026-01-08', NULL),
('D-998',  4, 'COMPLETED',     '2026-01-02', 'Pronto per riconsegna');
