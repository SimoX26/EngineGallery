DROP DATABASE IF EXISTS engine_gallery;
CREATE DATABASE engine_gallery CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE engine_gallery;


CREATE USER 'engine_gallery'@'localhost' IDENTIFIED BY 'engine123';

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


CREATE TABLE folders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT,

    CONSTRAINT fk_folder_parent
        FOREIGN KEY (parent_id)
        REFERENCES folders(id)
        ON DELETE CASCADE
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
    folder_id BIGINT,
    uploaded_by BIGINT,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_image_engine
        FOREIGN KEY (engine_id)
        REFERENCES engines(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_image_folder
        FOREIGN KEY (folder_id)
        REFERENCES folders(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_image_user
        FOREIGN KEY (uploaded_by)
        REFERENCES users(id)
        ON DELETE SET NULL
);





INSERT INTO users (username, password_hash, role) VALUES ('mario', SHA2('1234', 256),'INSPECTOR');
INSERT INTO users (username, password_hash, role) VALUES ('giordano', SHA2('1234', 256), 'INSPECTOR');
INSERT INTO users (username, password_hash, role) VALUES ('maurizio', SHA2('1234', 256), 'OPERATOR');
INSERT INTO users (username, password_hash, role) VALUES ('luigi', SHA2('1234', 256),'OPERATOR');
INSERT INTO users (username, password_hash, role) VALUES ('manuel', SHA2('1234', 256),'OPERATOR');
INSERT INTO users (username, password_hash, role)VALUES ('giggianuel', SHA2('1234', 256), 'INSPECTOR');


