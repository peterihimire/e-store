CREATE TABLE user_invitations (
                                  id BIGSERIAL PRIMARY KEY,

                                  slug VARCHAR(255) NOT NULL UNIQUE,

                                  email VARCHAR(255) NOT NULL,

                                  token_hash VARCHAR(255) NOT NULL,

                                  status VARCHAR(50) NOT NULL,

                                  expires_at TIMESTAMP NOT NULL,

                                  accepted_at TIMESTAMP,

                                  invited_by BIGINT,

                                  created_by VARCHAR(255),
                                  updated_by VARCHAR(255),

                                  created_at TIMESTAMP NOT NULL,
                                  updated_at TIMESTAMP,

                                  CONSTRAINT fk_user_invitations_user
                                      FOREIGN KEY(invited_by)
                                          REFERENCES users(id)
                                          ON DELETE SET NULL
);

CREATE INDEX idx_invitation_email
    ON user_invitations(email);

CREATE INDEX idx_invitation_token
    ON user_invitations(token_hash);

CREATE INDEX idx_invitation_status
    ON user_invitations(status);

CREATE INDEX idx_invitation_expiry
    ON user_invitations(expires_at);

CREATE INDEX idx_invitation_invited_by
    ON user_invitations(invited_by);