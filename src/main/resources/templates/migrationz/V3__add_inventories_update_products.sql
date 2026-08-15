ALTER TABLE products
DROP COLUMN inventory;

ALTER TABLE products
    ADD COLUMN sku VARCHAR(100);

UPDATE products
SET sku = 'SKU-' || id;

ALTER TABLE products
    ALTER COLUMN sku SET NOT NULL;

ALTER TABLE products
    ADD CONSTRAINT uk_products_sku UNIQUE (sku);


CREATE TABLE inventories
(
    id BIGSERIAL PRIMARY KEY,

    slug VARCHAR(255) NOT NULL UNIQUE,

    product_id BIGINT NOT NULL UNIQUE,

    total_stock INTEGER NOT NULL DEFAULT 0,

    reserved_stock INTEGER NOT NULL DEFAULT 0,

    damaged_stock INTEGER NOT NULL DEFAULT 0,

    reorder_level INTEGER NOT NULL DEFAULT 10,

    reorder_quantity INTEGER NOT NULL DEFAULT 50,

    version BIGINT,

    created_by VARCHAR(255),

    updated_by VARCHAR(255),

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP,

    CONSTRAINT fk_inventory_product
        FOREIGN KEY(product_id)
            REFERENCES products(id)
            ON DELETE CASCADE
);

CREATE INDEX idx_inventory_product
    ON inventories(product_id);

CREATE INDEX idx_inventory_available
    ON inventories(total_stock, reserved_stock);
