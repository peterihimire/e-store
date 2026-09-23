DROP INDEX IF EXISTS idx_settlement_item_allocation;

CREATE UNIQUE INDEX uk_settlement_item_allocation
    ON settlement_items (allocation_id);