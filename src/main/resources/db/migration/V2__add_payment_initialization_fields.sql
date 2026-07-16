ALTER TABLE payments
    ADD COLUMN authorization_url VARCHAR(500);

ALTER TABLE payments
    ADD COLUMN access_code VARCHAR(255);