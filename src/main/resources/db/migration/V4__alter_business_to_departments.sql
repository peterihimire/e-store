-- Add business relationship
ALTER TABLE departments
    ADD COLUMN business_id BIGINT;

-- Existing departments need a business before we can enforce NOT NULL.
-- Populate business_id here if existing department records belong to a
-- particular business.
--
-- Example:
-- UPDATE departments
-- SET business_id = 1
-- WHERE business_id IS NULL;

-- Remove the global uniqueness constraint on department name.
ALTER TABLE departments
DROP CONSTRAINT IF EXISTS departments_name_key;

-- Prevent duplicate department names within the same business.
ALTER TABLE departments
    ADD CONSTRAINT uk_department_business_name
        UNIQUE (business_id, name);

-- Add foreign key to businesses.
ALTER TABLE departments
    ADD CONSTRAINT fk_department_business
        FOREIGN KEY (business_id)
            REFERENCES businesses(id);

-- Once all existing departments have been assigned to a business,
-- make the relationship mandatory.
ALTER TABLE departments
    ALTER COLUMN business_id SET NOT NULL;

-- Index for tenant queries.
CREATE INDEX idx_departments_business_id
    ON departments(business_id);