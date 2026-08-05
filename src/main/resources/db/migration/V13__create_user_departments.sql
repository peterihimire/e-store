CREATE TABLE user_departments (
                                  user_id BIGINT NOT NULL,
                                  department_id BIGINT NOT NULL,

                                  PRIMARY KEY(user_id, department_id),

                                  CONSTRAINT fk_user_departments_user
                                      FOREIGN KEY(user_id)
                                          REFERENCES users(id)
                                          ON DELETE CASCADE,

                                  CONSTRAINT fk_user_departments_department
                                      FOREIGN KEY(department_id)
                                          REFERENCES departments(id)
                                          ON DELETE CASCADE
);
CREATE INDEX idx_user_departments_user
    ON user_departments(user_id);

CREATE INDEX idx_user_departments_department
    ON user_departments(department_id);