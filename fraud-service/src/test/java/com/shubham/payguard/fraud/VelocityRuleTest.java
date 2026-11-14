package com.shubham.payguard.fraud;

import static org.assertj.core.api.Assertions.assertThat;

import com.shubham.payguard.common.Channel;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class VelocityRuleTest {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
    private final VelocityRule rule = new VelocityRule(3, Duration.ofMinutes(1), clock);

    @Test
    void firesOnlyAfterLimitIsExceeded() {
        for (int i = 0; i < 3; i++) {
            assertThat(rule.evaluate(TestPayments.payment("10", "GB", Channel.ONLINE, "ACC1"))).isEmpty();
        }
        assertThat(rule.evaluate(TestPayments.payment("10", "GB", Channel.ONLINE, "ACC1")))
                .contains(new RuleHit("VELOCITY", 40));
    }

    @Test
    void oldPaymentsFallOutOfTheWindow() {
        for (int i = 0; i < 3; i++) {
            rule.evaluate(TestPayments.payment("10", "GB", Channel.ONLINE, "ACC1"));
        }
        clock.advance(Duration.ofMinutes(2));
        assertThat(rule.evaluate(TestPayments.payment("10", "GB", Channel.ONLINE, "ACC1"))).isEmpty();
    }

    @Test
    void accountsAreCountedIndependently() {
        for (int i = 0; i < 4; i++) {
            rule.evaluate(TestPayments.payment("10", "GB", Channel.ONLINE, "ACC1"));
        }
        assertThat(rule.evaluate(TestPayments.payment("10", "GB", Channel.ONLINE, "ACC2"))).isEmpty();
    }
}
