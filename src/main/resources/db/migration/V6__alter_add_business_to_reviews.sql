-- ============================================================
-- V24
-- Add business relationship to reviews
-- ============================================================


-- Add business_id as nullable initially so existing reviews
-- can be backfilled.
ALTER TABLE reviews
    ADD COLUMN business_id BIGINT;


-- Populate business_id from the product's business.
UPDATE reviews r
SET business_id = p.business_id
    FROM products p
WHERE r.product_id = p.id;


-- Ensure every existing review was successfully associated
-- with a business before making the column mandatory.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM reviews
        WHERE business_id IS NULL
    ) THEN
        RAISE EXCEPTION
            'Cannot make reviews.business_id NOT NULL: some reviews could not be associated with a business';
END IF;
END $$;


-- Business is now mandatory for every review.
ALTER TABLE reviews
    ALTER COLUMN business_id SET NOT NULL;


-- Add foreign key
ALTER TABLE reviews
    ADD CONSTRAINT fk_reviews_business
        FOREIGN KEY (business_id)
            REFERENCES businesses (id);


-- Index for tenant-scoped queries
CREATE INDEX idx_reviews_business_id
    ON reviews (business_id);


-- Index for queries such as:
-- "reviews for this product within this business"
CREATE INDEX idx_reviews_business_product
    ON reviews (business_id, product_id);