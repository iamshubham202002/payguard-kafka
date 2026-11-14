package com.shubham.payguard.fraud;

import com.shubham.payguard.common.Channel;
import com.shubham.payguard.common.PaymentInitiated;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

final class TestPayments {

    private TestPayments() {
    }

    static PaymentInitiated payment(String amount, String country, Channel channel, String debtor) {
        return new PaymentInitiated(UUID.randomUUID(), "E2E-" + UUID.randomUUID(), debtor, "GB00CREDITOR",
                new BigDecimal(amount), "GBP", channel, country, Instant.parse("2026-01-01T00:00:00Z"));
    }

    static PaymentInitiated payment(String amount) {
        return payment(amount, "GB", Channel.ONLINE, "GB00DEBTOR");
    }
}
