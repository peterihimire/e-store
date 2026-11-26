CREATE TABLE payout_recipients (
                                   id BIGSERIAL PRIMARY KEY,

                                   created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                   updated_at TIMESTAMP WITH TIME ZONE,

                                   bank_account_id BIGINT NOT NULL,
                                   provider VARCHAR(30) NOT NULL,
                                   recipient_code VARCHAR(100) NOT NULL,
                                   recipient_name VARCHAR(255),
                                   active BOOLEAN NOT NULL DEFAULT TRUE,

                                   CONSTRAINT fk_payout_recipient_bank_account
                                       FOREIGN KEY (bank_account_id)
                                           REFERENCES bank_accounts (id),

                                   CONSTRAINT uk_payout_recipient_bank_provider
                                       UNIQUE (bank_account_id, provider)
);

CREATE INDEX idx_payout_recipient_bank_account
    ON payout_recipients (bank_account_id);

CREATE INDEX idx_payout_recipient_provider
    ON payout_recipients (provider);

CREATE INDEX idx_payout_recipient_code
    ON payout_recipients (recipient_code);