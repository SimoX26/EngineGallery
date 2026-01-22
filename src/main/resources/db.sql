DROP DATABASE IF EXISTS engine_gallery;
CREATE DATABASE engine_gallery CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE engine_gallery;



CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('INSPECTOR', 'OPERATOR') NOT NULL
);



CREATE TABLE folders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT,
    FOREIGN KEY (parent_id) REFERENCES folders(id)
        ON DELETE CASCADE
);



CREATE TABLE engine (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    engine_code VARCHAR(50) NOT NULL UNIQUE,
    status ENUM('INTAKE', 'IN_PROGRESS', 'COMPLETED', 'DELIVERED') NOT NULL
);



CREATE TABLE image (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    engine_id BIGINT NOT NULL,
    file_path VARCHAR(255) NOT NULL,

    description TEXT,
    keywords VARCHAR(255),

    FOREIGN KEY (engine_id) REFERENCES engine(id)
        ON DELETE CASCADE
);



-- Ricerca rapida per codice motore
CREATE INDEX idx_engine_code
ON engine (engine_code);

-- Ricerca per stato motore
CREATE INDEX idx_engine_status
ON engine (status);

-- Ricerca keyword sulle immagini
CREATE INDEX idx_image_keywords
ON image (keywords);

CREATE FULLTEXT INDEX idx_image_description
ON image (description);




INSERT INTO users (username, password_hash, role)
VALUES (
    'mario',
    SHA2('1234', 256),
    'INSPECTOR'
);

INSERT INTO users (username, password_hash, role)
VALUES (
    'giordano',
    SHA2('1234', 256),
    'INSPECTOR'
);

INSERT INTO users (username, password_hash, role)
VALUES (
    'maurizio',
    SHA2('1234', 256),
    'OPERATOR'
);

INSERT INTO users (username, password_hash, role)
VALUES (
    'luigi',
    SHA2('1234', 256),
    'OPERATOR'
);

INSERT INTO users (username, password_hash, role)
VALUES (
    'manuel',
    SHA2('1234', 256),
    'OPERATOR'
);

INSERT INTO users (username, password_hash, role)
VALUES (
    'giggianuel',
    SHA2('1234', 256),
    'INSPECTOR'
);