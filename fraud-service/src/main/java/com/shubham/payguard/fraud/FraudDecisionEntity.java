package com.shubham.payguard.fraud;

import com.shubham.payguard.common.Decision;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Persisted decision. The payment id as primary key makes screening idempotent per payment. */
@Entity
@Table(name = "fraud_decisions")
public class FraudDecisionEntity {

    @Id
    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "debtor_account", nullable = false)
    private String debtorAccount;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Decision decision;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Column(name = "triggered_rules", nullable = false)
    private String triggeredRules;

    @Column(name = "decided_at", nullable = false)
    private Instant decidedAt;

    protected FraudDecisionEntity() {
        // for JPA
    }

    public FraudDecisionEntity(UUID paymentId, String debtorAccount, BigDecimal amount, Decision decision,
                               int riskScore, String triggeredRules, Instant decidedAt) {
        this.paymentId = paymentId;
        this.debtorAccount = debtorAccount;
        this.amount = amount;
        this.decision = decision;
        this.riskScore = riskScore;
        this.triggeredRules = triggeredRules;
        this.decidedAt = decidedAt;
    }

    public UUID getPaymentId() { return paymentId; }
    public String getDebtorAccount() { return debtorAccount; }
    public BigDecimal getAmount() { return amount; }
    public Decision getDecision() { return decision; }
    public int getRiskScore() { return riskScore; }
    public String getTriggeredRules() { return triggeredRules; }
    public Instant getDecidedAt() { return decidedAt; }
}
