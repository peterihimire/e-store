-- =========================================================
-- BUSINESS BALANCES
-- =========================================================

CREATE TABLE business_balances (
                                   id BIGSERIAL PRIMARY KEY,

                                   slug VARCHAR(255) NOT NULL UNIQUE,

                                   business_id BIGINT NOT NULL,

                                   currency VARCHAR(3) NOT NULL,

                                   pending_balance NUMERIC(19,2) NOT NULL DEFAULT 0,
                                   available_balance NUMERIC(19,2) NOT NULL DEFAULT 0,
                                   reserved_balance NUMERIC(19,2) NOT NULL DEFAULT 0,

                                   lifetime_gross NUMERIC(19,2) NOT NULL DEFAULT 0,
                                   lifetime_fees NUMERIC(19,2) NOT NULL DEFAULT 0,
                                   lifetime_payouts NUMERIC(19,2) NOT NULL DEFAULT 0,

                                   created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                   updated_at TIMESTAMP WITH TIME ZONE,

                                   version BIGINT NOT NULL DEFAULT 0,

                                   CONSTRAINT uk_business_balance_currency
                                       UNIQUE (business_id, currency),

                                   CONSTRAINT fk_business_balance_business
                                       FOREIGN KEY (business_id)
                                           REFERENCES businesses(id)
);

CREATE INDEX idx_business_balance_business
    ON business_balances(business_id);


-- =========================================================
-- LEDGER ACCOUNTS
-- =========================================================

CREATE TABLE ledger_accounts (
                                 id BIGSERIAL PRIMARY KEY,

                                 slug VARCHAR(255) NOT NULL UNIQUE,

                                 business_id BIGINT,

                                 account_type VARCHAR(50) NOT NULL,

                                 currency VARCHAR(3) NOT NULL,

                                 active BOOLEAN NOT NULL DEFAULT TRUE,

                                 description VARCHAR(255),

                                 created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                 updated_at TIMESTAMP WITH TIME ZONE,

                                 created_by VARCHAR(255),
                                 updated_by VARCHAR(255),

                                 CONSTRAINT fk_ledger_account_business
                                     FOREIGN KEY (business_id)
                                         REFERENCES businesses(id)
);

CREATE INDEX idx_ledger_account_business
    ON ledger_accounts(business_id);

CREATE INDEX idx_ledger_account_type
    ON ledger_accounts(account_type);


-- =========================================================
-- LEDGER ACCOUNT UNIQUENESS
-- =========================================================

-- One seller account of a given type/currency.
CREATE UNIQUE INDEX uk_ledger_account_business_type_currency
    ON ledger_accounts(business_id, account_type, currency)
    WHERE business_id IS NOT NULL;

-- One platform account of a given type/currency.
CREATE UNIQUE INDEX uk_ledger_account_platform_type_currency
    ON ledger_accounts(account_type, currency)
    WHERE business_id IS NULL;


-- =========================================================
-- LEDGER TRANSACTIONS
-- =========================================================

CREATE TABLE ledger_transactions (
                                     id BIGSERIAL PRIMARY KEY,

                                     slug VARCHAR(255) NOT NULL UNIQUE,

                                     transaction_type VARCHAR(50) NOT NULL,

                                     currency VARCHAR(3) NOT NULL,

                                     reference VARCHAR(100) NOT NULL UNIQUE,

                                     description VARCHAR(500),

                                     total_debit NUMERIC(19,2) NOT NULL DEFAULT 0,

                                     total_credit NUMERIC(19,2) NOT NULL DEFAULT 0,

                                     created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                     updated_at TIMESTAMP WITH TIME ZONE,

                                     created_by VARCHAR(255),
                                     updated_by VARCHAR(255)
);

CREATE INDEX idx_ledger_transaction_type
    ON ledger_transactions(transaction_type);

CREATE INDEX idx_ledger_transaction_created_at
    ON ledger_transactions(created_at);


-- =========================================================
-- SETTLEMENTS
-- =========================================================

CREATE TABLE settlements (
                             id BIGSERIAL PRIMARY KEY,

                             slug VARCHAR(255) NOT NULL UNIQUE,

                             settlement_number VARCHAR(255) NOT NULL UNIQUE,

                             business_id BIGINT NOT NULL,

                             currency VARCHAR(3) NOT NULL,

                             period_start TIMESTAMP WITH TIME ZONE NOT NULL,

                             period_end TIMESTAMP WITH TIME ZONE NOT NULL,

                             gross_amount NUMERIC(19,2) NOT NULL DEFAULT 0,

                             discount_amount NUMERIC(19,2) NOT NULL DEFAULT 0,

                             platform_fee NUMERIC(19,2) NOT NULL DEFAULT 0,

                             payment_fee NUMERIC(19,2) NOT NULL DEFAULT 0,

                             tax_amount NUMERIC(19,2) NOT NULL DEFAULT 0,

                             shipping_amount NUMERIC(19,2) NOT NULL DEFAULT 0,

                             refund_amount NUMERIC(19,2) NOT NULL DEFAULT 0,

                             adjustment_amount NUMERIC(19,2) NOT NULL DEFAULT 0,

                             net_amount NUMERIC(19,2) NOT NULL DEFAULT 0,

                             status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

                             eligible_at TIMESTAMP WITH TIME ZONE,

                             settled_at TIMESTAMP WITH TIME ZONE,

                             failure_reason VARCHAR(500),

                             created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                             updated_at TIMESTAMP WITH TIME ZONE,

                             created_by VARCHAR(255),

                             updated_by VARCHAR(255),

                             CONSTRAINT fk_settlement_business
                                 FOREIGN KEY (business_id)
                                     REFERENCES businesses(id)
);

CREATE INDEX idx_settlement_business
    ON settlements(business_id);

CREATE INDEX idx_settlement_status
    ON settlements(status);

CREATE INDEX idx_settlement_period
    ON settlements(period_start, period_end);


-- =========================================================
-- SETTLEMENT ITEMS
-- =========================================================

CREATE TABLE settlement_items (
                                  id BIGSERIAL PRIMARY KEY,

                                  slug VARCHAR(255) NOT NULL UNIQUE,

                                  settlement_id BIGINT NOT NULL,

                                  allocation_id BIGINT NOT NULL,

                                  business_id BIGINT NOT NULL,

                                  amount NUMERIC(19,2) NOT NULL,

                                  currency VARCHAR(3) NOT NULL,

                                  created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                  updated_at TIMESTAMP WITH TIME ZONE,

                                  created_by VARCHAR(255),

                                  updated_by VARCHAR(255),

                                  CONSTRAINT uk_settlement_allocation
                                      UNIQUE (settlement_id, allocation_id),

                                  CONSTRAINT fk_settlement_item_settlement
                                      FOREIGN KEY (settlement_id)
                                          REFERENCES settlements(id),

                                  CONSTRAINT fk_settlement_item_allocation
                                      FOREIGN KEY (allocation_id)
                                          REFERENCES allocations(id),

                                  CONSTRAINT fk_settlement_item_business
                                      FOREIGN KEY (business_id)
                                          REFERENCES businesses(id)
);

CREATE INDEX idx_settlement_item_allocation
    ON settlement_items(allocation_id);

CREATE INDEX idx_settlement_item_business
    ON settlement_items(business_id);


-- =========================================================
-- PAYOUTS
-- =========================================================

CREATE TABLE payouts (
                         id BIGSERIAL PRIMARY KEY,

                         slug VARCHAR(255) NOT NULL UNIQUE,

                         payout_number VARCHAR(255) NOT NULL UNIQUE,

                         business_id BIGINT NOT NULL,

                         bank_account_id BIGINT NOT NULL,

                         account_name VARCHAR(255) NOT NULL,

                         account_number VARCHAR(20) NOT NULL,

                         bank_code VARCHAR(20) NOT NULL,

                         bank_name VARCHAR(255) NOT NULL,

                         gross_amount NUMERIC(18,2) NOT NULL DEFAULT 0,

                         transfer_fee NUMERIC(18,2) NOT NULL DEFAULT 0,

                         stamp_duty NUMERIC(18,2) NOT NULL DEFAULT 0,

                         net_amount NUMERIC(18,2) NOT NULL DEFAULT 0,

                         status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',

                         currency VARCHAR(3) NOT NULL,

                         provider VARCHAR(50),

                         provider_reference VARCHAR(255) UNIQUE,

                         idempotency_key VARCHAR(100) NOT NULL UNIQUE,

                         failure_reason VARCHAR(500),

                         requested_at TIMESTAMP WITH TIME ZONE,

                         processed_at TIMESTAMP WITH TIME ZONE,

                         created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                         updated_at TIMESTAMP WITH TIME ZONE,

                         created_by VARCHAR(255),

                         updated_by VARCHAR(255),

                         CONSTRAINT fk_payout_business
                             FOREIGN KEY (business_id)
                                 REFERENCES businesses(id),

                         CONSTRAINT fk_payout_bank_account
                             FOREIGN KEY (bank_account_id)
                                 REFERENCES bank_accounts(id)
);

CREATE INDEX idx_payout_business
    ON payouts(business_id);

CREATE INDEX idx_payout_bank_account
    ON payouts(bank_account_id);

CREATE INDEX idx_payout_status
    ON payouts(status);

-- No separate index on provider_reference is necessary
-- because UNIQUE already creates an index.


-- =========================================================
-- PAYOUT ITEMS
-- =========================================================

CREATE TABLE payout_items (
                              id BIGSERIAL PRIMARY KEY,

                              slug VARCHAR(255) NOT NULL UNIQUE,

                              payout_id BIGINT NOT NULL,

                              settlement_id BIGINT NOT NULL,

                              business_id BIGINT NOT NULL,

                              amount NUMERIC(19,2) NOT NULL,

                              currency VARCHAR(3) NOT NULL,

                              created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                              updated_at TIMESTAMP WITH TIME ZONE,

                              CONSTRAINT uk_payout_settlement
                                  UNIQUE (payout_id, settlement_id),

                              CONSTRAINT fk_payout_item_payout
                                  FOREIGN KEY (payout_id)
                                      REFERENCES payouts(id),

                              CONSTRAINT fk_payout_item_settlement
                                  FOREIGN KEY (settlement_id)
                                      REFERENCES settlements(id),

                              CONSTRAINT fk_payout_item_business
                                  FOREIGN KEY (business_id)
                                      REFERENCES businesses(id)
);

CREATE INDEX idx_payout_item_settlement
    ON payout_items(settlement_id);

CREATE INDEX idx_payout_item_business
    ON payout_items(business_id);


-- =========================================================
-- LEDGER ENTRIES
-- =========================================================

CREATE TABLE ledger_entries (
                                id BIGSERIAL PRIMARY KEY,

                                slug VARCHAR(255) NOT NULL UNIQUE,

                                transaction_id BIGINT NOT NULL,

                                account_id BIGINT NOT NULL,

                                business_id BIGINT,

                                allocation_id BIGINT,

                                settlement_id BIGINT,

                                payout_id BIGINT,

                                direction VARCHAR(10) NOT NULL,

                                amount NUMERIC(19,2) NOT NULL,

                                currency VARCHAR(3) NOT NULL,

                                description VARCHAR(500),

                                created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                updated_at TIMESTAMP WITH TIME ZONE,

                                created_by VARCHAR(255),

                                updated_by VARCHAR(255),

                                CONSTRAINT fk_ledger_entry_transaction
                                    FOREIGN KEY (transaction_id)
                                        REFERENCES ledger_transactions(id),

                                CONSTRAINT fk_ledger_entry_account
                                    FOREIGN KEY (account_id)
                                        REFERENCES ledger_accounts(id),

                                CONSTRAINT fk_ledger_entry_business
                                    FOREIGN KEY (business_id)
                                        REFERENCES businesses(id),

                                CONSTRAINT fk_ledger_entry_allocation
                                    FOREIGN KEY (allocation_id)
                                        REFERENCES allocations(id),

                                CONSTRAINT fk_ledger_entry_settlement
                                    FOREIGN KEY (settlement_id)
                                        REFERENCES settlements(id),

                                CONSTRAINT fk_ledger_entry_payout
                                    FOREIGN KEY (payout_id)
                                        REFERENCES payouts(id)
);

CREATE INDEX idx_ledger_entry_account
    ON ledger_entries(account_id);

CREATE INDEX idx_ledger_entry_business
    ON ledger_entries(business_id);

CREATE INDEX idx_ledger_entry_allocation
    ON ledger_entries(allocation_id);

CREATE INDEX idx_ledger_entry_settlement
    ON ledger_entries(settlement_id);

CREATE INDEX idx_ledger_entry_payout
    ON ledger_entries(payout_id);

CREATE INDEX idx_ledger_entry_created_at
    ON ledger_entries(created_at);