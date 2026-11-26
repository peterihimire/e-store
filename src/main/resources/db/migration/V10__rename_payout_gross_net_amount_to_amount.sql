-- VXX__rename_payout_gross_net_amount_to_amount.sql

ALTER TABLE payouts
    RENAME COLUMN gross_amount TO amount;

ALTER TABLE payouts
DROP COLUMN net_amount;