CREATE TABLE password_reset_tokens (
                                       id BIGSERIAL PRIMARY KEY,

                                       slug VARCHAR(255) NOT NULL UNIQUE,

                                       token_hash VARCHAR(255) NOT NULL,

                                       user_id BIGINT NOT NULL,

                                       expires_at TIMESTAMP NOT NULL,

                                       used_at TIMESTAMP,

                                       created_at TIMESTAMP NOT NULL,

                                       updated_at TIMESTAMP,

                                       CONSTRAINT fk_password_reset_tokens_user
                                           FOREIGN KEY (user_id)
                                               REFERENCES users(id)
                                               ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_user
    ON password_reset_tokens(user_id);

CREATE INDEX idx_password_reset_slug
    ON password_reset_tokens(slug);