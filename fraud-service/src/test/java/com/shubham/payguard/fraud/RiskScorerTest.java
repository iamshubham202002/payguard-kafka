package com.shubham.payguard.fraud;

import static org.assertj.core.api.Assertions.assertThat;

import com.shubham.payguard.common.Decision;
import com.shubham.payguard.common.FraudDecision;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RiskScorerTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    private static FraudRule fires(String name, int score) {
        return payment -> Optional.of(new RuleHit(name, score));
    }

    private static FraudRule silent() {
        return payment -> Optional.empty();
    }

    private RiskScorer scorer(FraudRule... rules) {
        return new RiskScorer(List.of(rules), 40, 70, clock);
    }

    @Test
    void noRulesFiredIsApproved() {
        FraudDecision d = scorer(silent()).score(TestPayments.payment("10"));
        assertThat(d.decision()).isEqualTo(Decision.APPROVE);
        assertThat(d.riskScore()).isZero();
        assertThat(d.triggeredRules()).isEmpty();
    }

    @Test
    void scoreAtReviewThresholdIsReview() {
        FraudDecision d = scorer(fires("A", 40)).score(TestPayments.payment("10"));
        assertThat(d.decision()).isEqualTo(Decision.REVIEW);
    }

    @Test
    void scoreAtRejectThresholdIsReject() {
        FraudDecision d = scorer(fires("A", 70)).score(TestPayments.payment("10"));
        assertThat(d.decision()).isEqualTo(Decision.REJECT);
    }

    @Test
    void scoresAddUpAndAreCappedAtOneHundred() {
        FraudDecision d = scorer(fires("A", 60), fires("B", 60)).score(TestPayments.payment("10"));
        assertThat(d.riskScore()).isEqualTo(100);
        assertThat(d.triggeredRules()).containsExactly("A", "B");
    }

    @Test
    void everyRuleIsEvaluatedEvenAfterOneFires() {
        int[] calls = {0};
        FraudRule counting = payment -> {
            calls[0]++;
            return Optional.empty();
        };
        scorer(fires("A", 90), counting).score(TestPayments.payment("10"));
        assertThat(calls[0]).isEqualTo(1);
    }
}
