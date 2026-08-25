CREATE TABLE allocations (
                             id BIGSERIAL PRIMARY KEY,

                             slug VARCHAR(255) NOT NULL UNIQUE,

                             payment_id BIGINT NOT NULL,
                             order_item_id BIGINT NOT NULL,
                             business_id BIGINT NOT NULL,

                             gross_amount NUMERIC(18, 2) NOT NULL DEFAULT 0.00,
                             platform_fee NUMERIC(18, 2) NOT NULL DEFAULT 0.00,
                             payment_fee NUMERIC(18, 2) NOT NULL DEFAULT 0.00,
                             refund_amount NUMERIC(18, 2) NOT NULL DEFAULT 0.00,
                             net_amount NUMERIC(18, 2) NOT NULL DEFAULT 0.00,

                             discount_amount NUMERIC(18, 2) NOT NULL DEFAULT 0.00,
                             tax_amount NUMERIC(18, 2) NOT NULL DEFAULT 0.00,
                             shipping_amount NUMERIC(18, 2) NOT NULL DEFAULT 0.00,

                             currency VARCHAR(3) NOT NULL DEFAULT 'NGN',
                             status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

                             created_at TIMESTAMP NOT NULL,
                             updated_at TIMESTAMP,

                             CONSTRAINT fk_allocations_payment
                                 FOREIGN KEY (payment_id)
                                     REFERENCES payments(id),

                             CONSTRAINT fk_allocations_order_item
                                 FOREIGN KEY (order_item_id)
                                     REFERENCES order_items(id),

                             CONSTRAINT fk_allocations_business
                                 FOREIGN KEY (business_id)
                                     REFERENCES businesses(id)
);

CREATE INDEX idx_allocations_payment_id
    ON allocations(payment_id);

CREATE INDEX idx_allocations_order_item_id
    ON allocations(order_item_id);

CREATE INDEX idx_allocations_business_id
    ON allocations(business_id);

CREATE INDEX idx_allocations_business_created_at
    ON allocations(business_id, created_at);

