ALTER TABLE user_invitations
    ADD COLUMN business_id BIGINT NOT NULL;

ALTER TABLE user_invitations
    ADD CONSTRAINT fk_user_invitations_business
        FOREIGN KEY (business_id)
            REFERENCES businesses (id);

CREATE INDEX idx_invitation_business_id
    ON user_invitations (business_id);