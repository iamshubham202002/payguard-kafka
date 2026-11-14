package com.shubham.payguard.payment;

import com.shubham.payguard.common.PaymentInitiated;
import com.shubham.payguard.common.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Publishes to Kafka only after the database transaction commits, so a rolled-back payment is
 * never announced. Known gap: if the process dies between commit and send the event is lost;
 * a transactional outbox would close that (see README).
 */
@Component
public class PaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentAccepted(PaymentAccepted accepted) {
        PaymentInitiated event = accepted.event();
        // Key by debtor account so all payments of one account land on one partition, in order.
        kafkaTemplate.send(Topics.PAYMENT_INITIATED, event.debtorAccount(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish payment {}", event.paymentId(), ex);
                    } else {
                        log.info("Published payment {} to {}-{}@{}", event.paymentId(),
                                result.getRecordMetadata().topic(), result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
