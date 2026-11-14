package com.shubham.payguard.fraud;

import com.shubham.payguard.common.Decision;
import com.shubham.payguard.common.FraudDecision;
import com.shubham.payguard.common.PaymentInitiated;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Runs every rule, sums the points (capped at 100) and maps the total to a decision. */
@Component
public class RiskScorer {

    private final List<FraudRule> rules;
    private final int reviewThreshold;
    private final int rejectThreshold;
    private final Clock clock;

    public RiskScorer(List<FraudRule> rules,
                      @Value("${fraud.review-threshold:40}") int reviewThreshold,
                      @Value("${fraud.reject-threshold:70}") int rejectThreshold,
                      Clock clock) {
        this.rules = rules;
        this.reviewThreshold = reviewThreshold;
        this.rejectThreshold = rejectThreshold;
        this.clock = clock;
    }

    public FraudDecision score(PaymentInitiated payment) {
        // Every rule runs (some keep state), so do not short-circuit.
        List<RuleHit> hits = rules.stream()
                .map(rule -> rule.evaluate(payment))
                .flatMap(Optional::stream)
                .toList();
        int total = Math.min(100, hits.stream().mapToInt(RuleHit::score).sum());
        Decision decision = total >= rejectThreshold ? Decision.REJECT
                : total >= reviewThreshold ? Decision.REVIEW
                : Decision.APPROVE;
        return new FraudDecision(payment.paymentId(), decision, total,
                hits.stream().map(RuleHit::rule).toList(), clock.instant());
    }
}
