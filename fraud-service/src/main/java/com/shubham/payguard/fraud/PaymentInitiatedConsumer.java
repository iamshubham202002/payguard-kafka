package com.shubham.payguard.fraud;

import com.shubham.payguard.common.FraudDecision;
import com.shubham.payguard.common.PaymentInitiated;
import com.shubham.payguard.common.Topics;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentInitiatedConsumer {

    private final FraudDecisionService decisionService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentInitiatedConsumer(FraudDecisionService decisionService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.decisionService = decisionService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = Topics.PAYMENT_INITIATED, groupId = "fraud-service")
    public void onPayment(PaymentInitiated payment) {
        FraudDecision decision = decisionService.decide(payment);
        try {
            // Wait for the broker ack: if publishing fails the exception reaches the error handler,
            // the offset is not committed, and the record is retried, then dead-lettered.
            kafkaTemplate.send(Topics.PAYMENT_DECISIONS, payment.paymentId().toString(), decision)
                    .get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while publishing decision", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Could not publish decision for payment " + payment.paymentId(), e);
        }
    }
}
