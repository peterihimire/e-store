ALTER TABLE user_invitations
    ADD COLUMN first_name VARCHAR(100);

ALTER TABLE user_invitations
    ADD COLUMN last_name VARCHAR(100);

ALTER TABLE user_invitations
    ADD COLUMN phone_number VARCHAR(30);