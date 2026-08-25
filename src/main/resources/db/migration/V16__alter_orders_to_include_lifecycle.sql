ALTER TABLE orders
    ADD COLUMN shipped_at TIMESTAMP,
    ADD COLUMN delivered_at TIMESTAMP,
    ADD COLUMN paid_at TIMESTAMP,
    ADD COLUMN cancelled_at TIMESTAMP,
    ADD COLUMN completed_at TIMESTAMP;