INSERT INTO inventories (
    slug,
    product_id,
    total_stock,
    reserved_stock,
    damaged_stock,
    reorder_level,
    reorder_quantity,
    created_at
)
SELECT
    'INV-' || p.id,
    p.id,
    0,
    0,
    0,
    10,
    50,
    NOW()
FROM products p
WHERE NOT EXISTS (
    SELECT 1
    FROM inventories i
    WHERE i.product_id = p.id
);