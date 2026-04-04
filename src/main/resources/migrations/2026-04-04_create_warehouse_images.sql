CREATE TABLE IF NOT EXISTS warehouse_images (
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
