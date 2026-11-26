-- =========================================================
-- NOTIFICATION DISPATCH: AFTER-COMMIT OUTBOX + RETRY CLAIM
-- =========================================================

ALTER TABLE notifications ADD COLUMN next_attempt_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE notifications ADD COLUMN request_payload TEXT;

ALTER TABLE notifications ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

CREATE INDEX idx_notification_due
    ON notifications (status, next_attempt_at);

-- Rows written by an earlier version are due immediately so the retry worker can recover them.
UPDATE notifications
SET next_attempt_at = NOW()
WHERE status IN ('PENDING', 'FAILED');
