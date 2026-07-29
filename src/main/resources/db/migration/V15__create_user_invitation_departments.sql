CREATE TABLE user_invitation_departments (
                                             invitation_id BIGINT NOT NULL,
                                             department_id BIGINT NOT NULL,

                                             PRIMARY KEY(invitation_id, department_id),

                                             CONSTRAINT fk_uid_invitation
                                                 FOREIGN KEY(invitation_id)
                                                     REFERENCES user_invitations(id)
                                                     ON DELETE CASCADE,

                                             CONSTRAINT fk_uid_department
                                                 FOREIGN KEY(department_id)
                                                     REFERENCES departments(id)
                                                     ON DELETE CASCADE
);

CREATE INDEX idx_user_invitation_departments_invitation
    ON user_invitation_departments(invitation_id);

CREATE INDEX idx_user_invitation_departments_department
    ON user_invitation_departments(department_id);