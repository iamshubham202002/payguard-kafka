package com.shubham.payguard.payment;

import com.shubham.payguard.common.FraudDecision;
import com.shubham.payguard.common.Topics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FraudDecisionListener {

    private final PaymentService paymentService;

    public FraudDecisionListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = Topics.PAYMENT_DECISIONS, groupId = "payment-service")
    public void onDecision(FraudDecision decision) {
        paymentService.applyDecision(decision);
    }
}
