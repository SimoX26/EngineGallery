CREATE TABLE IF NOT EXISTS hydraulic_tests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_name VARCHAR(100) NOT NULL,
    engine_code VARCHAR(80) NOT NULL,
    video_url VARCHAR(500) NOT NULL,
    test_date DATE NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
