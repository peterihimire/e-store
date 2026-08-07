CREATE TABLE refunds (
                         id BIGSERIAL PRIMARY KEY,

                         slug VARCHAR(255) NOT NULL UNIQUE,

                         user_id BIGINT NOT NULL,
                         payment_id BIGINT NOT NULL,

                         amount NUMERIC(19,2) NOT NULL,
                         currency VARCHAR(20) NOT NULL,
                         status VARCHAR(50) NOT NULL,
                         provider VARCHAR(50) NOT NULL,

                         reference VARCHAR(255) NOT NULL UNIQUE,
                         gateway_refund_id VARCHAR(255) UNIQUE,

                         reason TEXT,
                         gateway_response TEXT,
                         failure_reason TEXT,

                         bank_code VARCHAR(100),
                         account_number VARCHAR(100),
                         account_name VARCHAR(255),

                         refunded_at TIMESTAMP,

                         created_at TIMESTAMP,
                         updated_at TIMESTAMP,
                         created_by VARCHAR(255),
                         updated_by VARCHAR(255),

                         CONSTRAINT fk_refund_user
                             FOREIGN KEY (user_id)
                                 REFERENCES users(id),

                         CONSTRAINT fk_refund_payment
                             FOREIGN KEY (payment_id)
                                 REFERENCES payments(id)
);