package com.shubham.payguard.payment;

import com.shubham.payguard.common.Channel;
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

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    private UUID id;

    @Column(name = "end_to_end_id", nullable = false, unique = true)
    private String endToEndId;

    @Column(name = "debtor_account", nullable = false)
    private String debtorAccount;

    @Column(name = "creditor_account", nullable = false)
    private String creditorAccount;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    @Column(name = "creditor_country", nullable = false)
    private String creditorCountry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Payment() {
        // for JPA
    }

    public Payment(UUID id, String endToEndId, String debtorAccount, String creditorAccount,
                   BigDecimal amount, String currency, Channel channel, String creditorCountry, Instant now) {
        this.id = id;
        this.endToEndId = endToEndId;
        this.debtorAccount = debtorAccount;
        this.creditorAccount = creditorAccount;
        this.amount = amount;
        this.currency = currency;
        this.channel = channel;
        this.creditorCountry = creditorCountry;
        this.status = PaymentStatus.RECEIVED;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /** Applies a fraud decision. Only a payment still in RECEIVED can be decided; returns false otherwise. */
    public boolean applyDecision(Decision decision, int score, Instant now) {
        if (status != PaymentStatus.RECEIVED) {
            return false;
        }
        this.status = switch (decision) {
            case APPROVE -> PaymentStatus.APPROVED;
            case REVIEW -> PaymentStatus.PENDING_REVIEW;
            case REJECT -> PaymentStatus.REJECTED;
        };
        this.riskScore = score;
        this.updatedAt = now;
        return true;
    }

    public UUID getId() { return id; }
    public String getEndToEndId() { return endToEndId; }
    public String getDebtorAccount() { return debtorAccount; }
    public String getCreditorAccount() { return creditorAccount; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public Channel getChannel() { return channel; }
    public String getCreditorCountry() { return creditorCountry; }
    public PaymentStatus getStatus() { return status; }
    public Integer getRiskScore() { return riskScore; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
