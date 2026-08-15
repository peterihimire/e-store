ALTER TABLE users
    ADD COLUMN account_type VARCHAR(20);

UPDATE users
SET account_type = 'PERSONAL'
WHERE account_type IS NULL;

ALTER TABLE users
    ALTER COLUMN account_type SET NOT NULL;