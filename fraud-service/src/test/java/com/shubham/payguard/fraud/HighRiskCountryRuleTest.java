package com.shubham.payguard.fraud;

import static org.assertj.core.api.Assertions.assertThat;

import com.shubham.payguard.common.Channel;
import java.util.Set;
import org.junit.jupiter.api.Test;

class HighRiskCountryRuleTest {

    private final HighRiskCountryRule rule = new HighRiskCountryRule(Set.of("KP", "IR"));

    @Test
    void firesForListedCountry() {
        assertThat(rule.evaluate(TestPayments.payment("100", "IR", Channel.ONLINE, "ACC1")))
                .contains(new RuleHit("HIGH_RISK_COUNTRY", 60));
    }

    @Test
    void ignoresOtherCountries() {
        assertThat(rule.evaluate(TestPayments.payment("100", "GB", Channel.ONLINE, "ACC1"))).isEmpty();
    }
}
