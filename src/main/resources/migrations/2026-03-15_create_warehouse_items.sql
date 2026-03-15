CREATE TABLE IF NOT EXISTS warehouse_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    sku VARCHAR(80),
    quantity INT NOT NULL DEFAULT 0,
    location VARCHAR(120),
    notes TEXT
);
