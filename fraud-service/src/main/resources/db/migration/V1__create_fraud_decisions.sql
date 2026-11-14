CREATE TABLE fraud_decisions (
    payment_id      UUID          PRIMARY KEY,
    debtor_account  VARCHAR(34)   NOT NULL,
    amount          NUMERIC(17,2) NOT NULL,
    decision        VARCHAR(10)   NOT NULL,
    risk_score      INTEGER       NOT NULL,
    triggered_rules VARCHAR(255)  NOT NULL DEFAULT '',
    decided_at      TIMESTAMPTZ   NOT NULL
);

CREATE INDEX idx_fraud_decisions_decision ON fraud_decisions (decision);
