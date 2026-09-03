-- V__add_package_dimensions_to_product_variants.sql

ALTER TABLE product_variants
    ADD COLUMN IF NOT EXISTS weight_kg NUMERIC(10, 3),
    ADD COLUMN IF NOT EXISTS length_cm NUMERIC(10, 2),
    ADD COLUMN IF NOT EXISTS width_cm NUMERIC(10, 2),
    ADD COLUMN IF NOT EXISTS height_cm NUMERIC(10, 2);