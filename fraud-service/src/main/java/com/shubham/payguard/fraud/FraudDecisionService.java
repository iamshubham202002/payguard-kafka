package com.shubham.payguard.fraud;

import com.shubham.payguard.common.FraudDecision;
import com.shubham.payguard.common.PaymentInitiated;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FraudDecisionService {

    private static final Logger log = LoggerFactory.getLogger(FraudDecisionService.class);

    private final RiskScorer scorer;
    private final FraudDecisionRepository repository;

    public FraudDecisionService(RiskScorer scorer, FraudDecisionRepository repository) {
        this.scorer = scorer;
        this.repository = repository;
    }

    /**
     * Kafka delivers at-least-once, so the same payment can arrive twice. The first delivery is
     * scored and stored; a redelivery returns the stored decision instead of re-scoring
     * (which would also double-count the velocity window).
     */
    @Transactional
    public FraudDecision decide(PaymentInitiated payment) {
        return repository.findById(payment.paymentId())
                .map(this::toDecision)
                .orElseGet(() -> {
                    FraudDecision decision = scorer.score(payment);
                    repository.save(new FraudDecisionEntity(
                            payment.paymentId(), payment.debtorAccount(), payment.amount(),
                            decision.decision(), decision.riskScore(),
                            String.join(",", decision.triggeredRules()), decision.decidedAt()));
                    log.info("Payment {} screened: {} (score {}, rules {})", payment.paymentId(),
                            decision.decision(), decision.riskScore(), decision.triggeredRules());
                    return decision;
                });
    }

    private FraudDecision toDecision(FraudDecisionEntity e) {
        List<String> rules = e.getTriggeredRules().isEmpty()
                ? List.of() : Arrays.asList(e.getTriggeredRules().split(","));
        return new FraudDecision(e.getPaymentId(), e.getDecision(), e.getRiskScore(), rules, e.getDecidedAt());
    }
}
