CREATE TABLE payments (
    id               UUID          PRIMARY KEY,
    end_to_end_id    VARCHAR(35)   NOT NULL UNIQUE,
    debtor_account   VARCHAR(34)   NOT NULL,
    creditor_account VARCHAR(34)   NOT NULL,
    amount           NUMERIC(17,2) NOT NULL,
    currency         VARCHAR(3)    NOT NULL,
    channel          VARCHAR(16)   NOT NULL,
    creditor_country VARCHAR(2)    NOT NULL,
    status           VARCHAR(20)   NOT NULL,
    risk_score       INTEGER,
    created_at       TIMESTAMPTZ   NOT NULL,
    updated_at       TIMESTAMPTZ   NOT NULL
);

CREATE INDEX idx_payments_debtor_account ON payments (debtor_account);
CREATE INDEX idx_payments_status ON payments (status);
