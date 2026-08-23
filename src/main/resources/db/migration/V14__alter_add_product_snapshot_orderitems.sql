ALTER TABLE order_items
    ADD COLUMN name VARCHAR(255),
    ADD COLUMN sku VARCHAR(255),
    ADD COLUMN brand VARCHAR(255);

UPDATE order_items oi
SET
    name = p.name,
    sku = p.sku,
    brand = p.brand
    FROM products p
WHERE oi.product_id = p.id;

ALTER TABLE order_items
    ALTER COLUMN name SET NOT NULL,
ALTER COLUMN sku SET NOT NULL;