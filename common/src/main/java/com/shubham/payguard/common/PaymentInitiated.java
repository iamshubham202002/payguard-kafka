package com.shubham.payguard.common;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Event published when a payment is accepted. Field names loosely follow ISO 20022 concepts
 * (end-to-end id, debtor, creditor) but this is NOT a full pacs.008 message.
 */
public record PaymentInitiated(
        UUID paymentId,
        String endToEndId,
        String debtorAccount,
        String creditorAccount,
        BigDecimal amount,
        String currency,
        Channel channel,
        String creditorCountry,
        Instant initiatedAt) {
}
