package com.shubham.payguard.fraud;

import com.shubham.payguard.common.FraudDecision;
import com.shubham.payguard.common.PaymentInitiated;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Used when {@code aws.enabled} is false (the default), so the service runs with Kafka + Postgres only. */
@Component
@ConditionalOnProperty(name = "aws.enabled", havingValue = "false", matchIfMissing = true)
public class NoopDecisionSink implements DecisionSink {

    @Override
    public void publish(PaymentInitiated payment, FraudDecision decision) {
        // nothing to do
    }
}
