CREATE TABLE refund_items (
                              id BIGSERIAL PRIMARY KEY,
                              slug VARCHAR(255) NOT NULL UNIQUE,

                              refund_id BIGINT NOT NULL,
                              order_item_id BIGINT NOT NULL,

                              quantity INTEGER NOT NULL,
                              amount NUMERIC(19, 2) NOT NULL,

                              created_at TIMESTAMP NOT NULL,
                              updated_at TIMESTAMP,

                              CONSTRAINT fk_refund_items_refund
                                  FOREIGN KEY (refund_id)
                                      REFERENCES refunds(id),

                              CONSTRAINT fk_refund_items_order_item
                                  FOREIGN KEY (order_item_id)
                                      REFERENCES order_items(id),

                              CONSTRAINT uq_refund_item
                                  UNIQUE (refund_id, order_item_id),

                              CONSTRAINT chk_refund_item_quantity
                                  CHECK (quantity > 0),

                              CONSTRAINT chk_refund_item_amount
                                  CHECK (amount >= 0)
);