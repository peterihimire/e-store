ALTER TABLE payout_recipients
    ADD COLUMN slug VARCHAR(255) NOT NULL,
    ADD COLUMN created_by VARCHAR(255),
    ADD COLUMN updated_by VARCHAR(255);

ALTER TABLE payout_recipients
    ADD CONSTRAINT uk_payout_recipient_slug
        UNIQUE (slug);