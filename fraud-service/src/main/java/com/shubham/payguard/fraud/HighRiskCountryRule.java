package com.shubham.payguard.fraud;

import com.shubham.payguard.common.PaymentInitiated;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Flags payments to configured high-risk beneficiary countries (ISO 3166 alpha-2). */
@Component
public class HighRiskCountryRule implements FraudRule {

    private final Set<String> highRiskCountries;

    public HighRiskCountryRule(@Value("${fraud.rules.high-risk-countries:KP,IR,SY}") Set<String> highRiskCountries) {
        this.highRiskCountries = highRiskCountries;
    }

    @Override
    public Optional<RuleHit> evaluate(PaymentInitiated payment) {
        return highRiskCountries.contains(payment.creditorCountry())
                ? Optional.of(new RuleHit("HIGH_RISK_COUNTRY", 60))
                : Optional.empty();
    }
}
