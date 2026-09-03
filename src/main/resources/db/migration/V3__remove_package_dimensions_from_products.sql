-- V__remove_package_dimensions_from_products.sql

ALTER TABLE products
DROP COLUMN IF EXISTS weight_kg,
    DROP COLUMN IF EXISTS length_cm,
    DROP COLUMN IF EXISTS width_cm,
    DROP COLUMN IF EXISTS height_cm;