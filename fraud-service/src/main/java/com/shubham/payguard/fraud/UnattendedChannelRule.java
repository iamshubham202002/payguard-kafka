package com.shubham.payguard.fraud;

import com.shubham.payguard.common.Channel;
import com.shubham.payguard.common.PaymentInitiated;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Adds risk to sizeable payments from unattended (API) channels, where no person is present to challenge. */
@Component
public class UnattendedChannelRule implements FraudRule {

    private final BigDecimal threshold;

    public UnattendedChannelRule(@Value("${fraud.rules.unattended.threshold:5000}") BigDecimal threshold) {
        this.threshold = threshold;
    }

    @Override
    public Optional<RuleHit> evaluate(PaymentInitiated payment) {
        if (payment.channel() == Channel.API && payment.amount().compareTo(threshold) >= 0) {
            return Optional.of(new RuleHit("UNATTENDED_CHANNEL_LARGE", 20));
        }
        return Optional.empty();
    }
}
