-- =========================================================
-- NOTIFICATION DELIVERY TRACKING
-- =========================================================

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(255) NOT NULL UNIQUE,

    event_key VARCHAR(255) NOT NULL UNIQUE,
    event_type VARCHAR(100),
    channel VARCHAR(20) NOT NULL DEFAULT 'EMAIL',
    recipient VARCHAR(255) NOT NULL,
    template VARCHAR(255),
    subject VARCHAR(500),
    body TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    failure_reason TEXT,
    sent_at TIMESTAMP WITH TIME ZONE,
    provider_message_id VARCHAR(255),
    attempt_count INTEGER NOT NULL DEFAULT 0,
    metadata TEXT,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_notification_status
    ON notifications (status);

CREATE INDEX idx_notification_recipient
    ON notifications (recipient);


-- =========================================================
-- DELIVERY ATTEMPTS
-- =========================================================

CREATE TABLE delivery_attempts (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(255) NOT NULL UNIQUE,

    notification_id BIGINT NOT NULL,
    attempt_number INTEGER NOT NULL,
    outcome VARCHAR(30) NOT NULL,
    provider VARCHAR(50),
    provider_message_id VARCHAR(255),
    error_message TEXT,
    duration_ms BIGINT,
    attempted_at TIMESTAMP WITH TIME ZONE NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_delivery_attempt_notification
        FOREIGN KEY (notification_id) REFERENCES notifications (id)
);

CREATE INDEX idx_delivery_attempt_notification
    ON delivery_attempts (notification_id);

CREATE INDEX idx_delivery_attempt_outcome
    ON delivery_attempts (outcome);
