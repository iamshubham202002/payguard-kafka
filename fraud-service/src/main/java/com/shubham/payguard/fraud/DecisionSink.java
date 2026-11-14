package com.shubham.payguard.fraud;

import com.shubham.payguard.common.FraudDecision;
import com.shubham.payguard.common.PaymentInitiated;

/** Side effects of a fraud decision outside Kafka (archive, analyst queue). */
public interface DecisionSink {

    void publish(PaymentInitiated payment, FraudDecision decision);
}
