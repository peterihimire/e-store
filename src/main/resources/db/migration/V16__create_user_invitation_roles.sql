CREATE TABLE user_invitation_roles (
                                       invitation_id BIGINT NOT NULL,
                                       role_id BIGINT NOT NULL,

                                       PRIMARY KEY(invitation_id, role_id),

                                       CONSTRAINT fk_uir_invitation
                                           FOREIGN KEY(invitation_id)
                                               REFERENCES user_invitations(id)
                                               ON DELETE CASCADE,

                                       CONSTRAINT fk_uir_role
                                           FOREIGN KEY(role_id)
                                               REFERENCES roles(id)
                                               ON DELETE CASCADE
);
CREATE INDEX idx_user_invitation_roles_invitation
    ON user_invitation_roles(invitation_id);

CREATE INDEX idx_user_invitation_roles_role
    ON user_invitation_roles(role_id);