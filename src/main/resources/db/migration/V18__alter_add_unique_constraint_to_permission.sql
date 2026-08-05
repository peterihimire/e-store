DROP INDEX IF EXISTS idx_permissions_resource_action;

ALTER TABLE permissions
    ADD CONSTRAINT uk_permission_resource_action
        UNIQUE (resource, action);