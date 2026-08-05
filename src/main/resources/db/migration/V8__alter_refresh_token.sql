-- Rename token -> token_hash
ALTER TABLE refresh_tokens
    RENAME COLUMN token TO token_hash;

-- Remove no longer needed columns
ALTER TABLE refresh_tokens
DROP COLUMN expired,
DROP COLUMN created_by,
DROP COLUMN updated_by;

-- Add new columns
ALTER TABLE refresh_tokens
    ADD COLUMN last_used_at TIMESTAMP,
ADD COLUMN revoked_at TIMESTAMP,
ADD COLUMN revoked_reason VARCHAR(255);

-- Update token_hash type (optional)
ALTER TABLE refresh_tokens
ALTER COLUMN token_hash TYPE TEXT;

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_refresh_user
    ON refresh_tokens(user_id);

CREATE INDEX IF NOT EXISTS idx_refresh_expires
    ON refresh_tokens(expires_at);

