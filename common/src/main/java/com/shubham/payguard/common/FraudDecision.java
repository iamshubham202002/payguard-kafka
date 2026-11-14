package com.shubham.payguard.common;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Outcome of fraud screening for one payment. */
public record FraudDecision(
        UUID paymentId,
        Decision decision,
        int riskScore,
        List<String> triggeredRules,
        Instant decidedAt) {
}
