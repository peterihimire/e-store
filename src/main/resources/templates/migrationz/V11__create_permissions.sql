CREATE TABLE permissions (
                             id BIGSERIAL PRIMARY KEY,
                             slug VARCHAR(255) NOT NULL UNIQUE,

                             name VARCHAR(255) NOT NULL UNIQUE,
                             description TEXT,

                             resource VARCHAR(255) NOT NULL,
                             action VARCHAR(255) NOT NULL,

                             created_by VARCHAR(255),
                             updated_by VARCHAR(255),

                             created_at TIMESTAMP NOT NULL,
                             updated_at TIMESTAMP
);

CREATE INDEX idx_permissions_resource_action
    ON permissions(resource, action);