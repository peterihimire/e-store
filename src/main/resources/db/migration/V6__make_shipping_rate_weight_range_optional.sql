-- V__make_shipping_rate_weight_range_optional.sql

ALTER TABLE shipping_rates
    ALTER COLUMN min_weight_kg DROP NOT NULL;