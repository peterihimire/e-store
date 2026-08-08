ALTER TABLE refunds
    RENAME COLUMN status TO refund_status;

ALTER TABLE refunds
    ADD COLUMN gateway_status VARCHAR(50) NOT NULL DEFAULT 'PENDING';