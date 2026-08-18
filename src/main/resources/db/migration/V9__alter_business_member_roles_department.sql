ALTER TABLE business_members
DROP CONSTRAINT IF EXISTS fkdcnsxj2wp0giyggldgxxcif26;

ALTER TABLE business_members
DROP CONSTRAINT IF EXISTS fkoyb71ryycawv2ceab6h6lp4d1;

ALTER TABLE business_members
DROP COLUMN IF EXISTS role_id;

ALTER TABLE business_members
DROP COLUMN IF EXISTS department_id;

ALTER TABLE business_members
DROP CONSTRAINT IF EXISTS uk_business_user;

ALTER TABLE business_members
    ADD CONSTRAINT uk_business_member_user UNIQUE (user_id);