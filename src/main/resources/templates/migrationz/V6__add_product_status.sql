ALTER TABLE products
    ADD COLUMN status VARCHAR(20);

UPDATE products
SET status = 'DRAFT'
WHERE status IS NULL;

ALTER TABLE products
    ALTER COLUMN status SET NOT NULL;