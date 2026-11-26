-- V__add_category_to_shipping_rates.sql

ALTER TABLE shipping_rates
    ADD COLUMN IF NOT EXISTS category_id BIGINT;

ALTER TABLE shipping_rates
    ADD CONSTRAINT fk_shipping_rates_category
        FOREIGN KEY (category_id)
            REFERENCES categories(id);