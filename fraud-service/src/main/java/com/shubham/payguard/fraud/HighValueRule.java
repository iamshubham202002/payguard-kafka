package com.shubham.payguard.fraud;

import com.shubham.payguard.common.PaymentInitiated;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Flags large payments. Thresholds are in the payment's own currency (no FX conversion in this demo). */
@Component
public class HighValueRule implements FraudRule {

    private final BigDecimal elevated;
    private final BigDecimal extreme;

    public HighValueRule(@Value("${fraud.rules.high-value.elevated:10000}") BigDecimal elevated,
                         @Value("${fraud.rules.high-value.extreme:50000}") BigDecimal extreme) {
        this.elevated = elevated;
        this.extreme = extreme;
    }

    @Override
    public Optional<RuleHit> evaluate(PaymentInitiated payment) {
        if (payment.amount().compareTo(extreme) >= 0) {
            return Optional.of(new RuleHit("HIGH_VALUE_EXTREME", 70));
        }
        if (payment.amount().compareTo(elevated) >= 0) {
            return Optional.of(new RuleHit("HIGH_VALUE", 40));
        }
        return Optional.empty();
    }
}
