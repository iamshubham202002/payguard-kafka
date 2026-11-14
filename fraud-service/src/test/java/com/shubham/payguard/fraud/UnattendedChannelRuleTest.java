package com.shubham.payguard.fraud;

import static org.assertj.core.api.Assertions.assertThat;

import com.shubham.payguard.common.Channel;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class UnattendedChannelRuleTest {

    private final UnattendedChannelRule rule = new UnattendedChannelRule(new BigDecimal("5000"));

    @Test
    void largeApiPaymentFires() {
        assertThat(rule.evaluate(TestPayments.payment("5000", "GB", Channel.API, "ACC1")))
                .contains(new RuleHit("UNATTENDED_CHANNEL_LARGE", 20));
    }

    @Test
    void smallApiPaymentDoesNotFire() {
        assertThat(rule.evaluate(TestPayments.payment("4999", "GB", Channel.API, "ACC1"))).isEmpty();
    }

    @Test
    void largeBranchPaymentDoesNotFire() {
        assertThat(rule.evaluate(TestPayments.payment("9000", "GB", Channel.BRANCH, "ACC1"))).isEmpty();
    }
}
