package com.shubham.payguard.payment;

import com.shubham.payguard.common.Channel;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        String endToEndId,
        BigDecimal amount,
        String currency,
        Channel channel,
        PaymentStatus status,
        Integer riskScore,
        Instant createdAt,
        Instant updatedAt) {

    static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.getId(), p.getEndToEndId(), p.getAmount(), p.getCurrency(),
                p.getChannel(), p.getStatus(), p.getRiskScore(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
