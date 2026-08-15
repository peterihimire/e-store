CREATE TABLE departments (
                             id BIGSERIAL PRIMARY KEY,
                             slug VARCHAR(255) NOT NULL UNIQUE,
                             name VARCHAR(255) NOT NULL UNIQUE,
                             description TEXT,

                             created_by VARCHAR(255),
                             updated_by VARCHAR(255),

                             created_at TIMESTAMP NOT NULL,
                             updated_at TIMESTAMP
);