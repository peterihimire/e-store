CREATE TABLE business_member_roles (
                                       business_member_id BIGINT NOT NULL,
                                       role_id BIGINT NOT NULL,

                                       CONSTRAINT fk_business_member_roles_member
                                           FOREIGN KEY (business_member_id)
                                               REFERENCES business_members(id)
                                               ON DELETE CASCADE,

                                       CONSTRAINT fk_business_member_roles_role
                                           FOREIGN KEY (role_id)
                                               REFERENCES roles(id)
                                               ON DELETE CASCADE,

                                       CONSTRAINT uk_business_member_roles
                                           UNIQUE (business_member_id, role_id)
);

CREATE INDEX idx_business_member_roles_member
    ON business_member_roles (business_member_id);

CREATE INDEX idx_business_member_roles_role
    ON business_member_roles (role_id);


CREATE TABLE business_member_departments (
                                             business_member_id BIGINT NOT NULL,
                                             department_id BIGINT NOT NULL,

                                             CONSTRAINT fk_business_member_departments_member
                                                 FOREIGN KEY (business_member_id)
                                                     REFERENCES business_members(id)
                                                     ON DELETE CASCADE,

                                             CONSTRAINT fk_business_member_departments_department
                                                 FOREIGN KEY (department_id)
                                                     REFERENCES departments(id)
                                                     ON DELETE CASCADE,

                                             CONSTRAINT uk_business_member_departments
                                                 UNIQUE (business_member_id, department_id)
);

CREATE INDEX idx_business_member_departments_member
    ON business_member_departments (business_member_id);

CREATE INDEX idx_business_member_departments_department
    ON business_member_departments (department_id);