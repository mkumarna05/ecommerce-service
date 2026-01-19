
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    CONSTRAINT chk_price_positive CHECK (price >= 0),
    CONSTRAINT chk_quantity_positive CHECK (quantity >= 0)
);

CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_deleted ON products(deleted);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_name_price ON products(name, price);
CREATE INDEX idx_products_deleted_quantity ON products(deleted, quantity);