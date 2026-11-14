package com.shubham.payguard.fraud;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class HighValueRuleTest {

    private final HighValueRule rule = new HighValueRule(new BigDecimal("10000"), new BigDecimal("50000"));

    @Test
    void belowThresholdDoesNotFire() {
        assertThat(rule.evaluate(TestPayments.payment("9999.99"))).isEmpty();
    }

    @Test
    void atElevatedThresholdFires() {
        assertThat(rule.evaluate(TestPayments.payment("10000.00")))
                .contains(new RuleHit("HIGH_VALUE", 40));
    }

    @Test
    void atExtremeThresholdFiresHigherScore() {
        assertThat(rule.evaluate(TestPayments.payment("50000.00")))
                .contains(new RuleHit("HIGH_VALUE_EXTREME", 70));
    }
}
