-- ============================================================
-- Update roles to support system-level and business-level roles
-- ============================================================


-- Add business relationship
ALTER TABLE roles
    ADD COLUMN business_id BIGINT;


-- Add foreign key
ALTER TABLE roles
    ADD CONSTRAINT fk_roles_business
        FOREIGN KEY (business_id)
            REFERENCES businesses (id);


-- Remove the existing global unique constraint on role name.
-- The exact constraint name may differ depending on how the
-- original table was created.
ALTER TABLE roles
DROP CONSTRAINT IF EXISTS roles_name_key;


-- Index for business-scoped role queries
CREATE INDEX idx_roles_business_id
    ON roles (business_id);


-- ============================================================
-- Business roles
-- ============================================================
--
-- A business can have only one role with a given name.
--
-- Business A:
--   ADMIN
--   SALES
--
-- Business B:
--   ADMIN
--   SALES
--
-- This is allowed because the business_id is different.
--

CREATE UNIQUE INDEX uk_role_business_name
    ON roles (business_id, name)
    WHERE business_id IS NOT NULL;


-- ============================================================
-- System roles
-- ============================================================
--
-- System roles have business_id = NULL.
--
-- This prevents duplicate system roles such as:
--
-- SUPER_ADMIN
-- SUPER_ADMIN
--
-- ADMIN
-- ADMIN
--

CREATE UNIQUE INDEX uk_system_role_name
    ON roles (name)
    WHERE business_id IS NULL;