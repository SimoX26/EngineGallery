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
