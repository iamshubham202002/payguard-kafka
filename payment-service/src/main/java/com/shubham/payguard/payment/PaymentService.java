package com.shubham.payguard.payment;

import com.shubham.payguard.common.FraudDecision;
import com.shubham.payguard.common.PaymentInitiated;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    public record CreationResult(Payment payment, boolean created) {
    }

    private final PaymentRepository repository;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public PaymentService(PaymentRepository repository, ApplicationEventPublisher events, Clock clock) {
        this.repository = repository;
        this.events = events;
        this.clock = clock;
    }

    /**
     * Idempotent on endToEndId: a retry of the same request returns the original payment
     * and does not publish a second event.
     */
    @Transactional
    public CreationResult create(PaymentRequest request) {
        var existing = repository.findByEndToEndId(request.endToEndId());
        if (existing.isPresent()) {
            return new CreationResult(existing.get(), false);
        }
        Instant now = clock.instant();
        Payment payment = repository.save(new Payment(
                UUID.randomUUID(), request.endToEndId(), request.debtorAccount(), request.creditorAccount(),
                request.amount(), request.currency(), request.channel(), request.creditorCountry(), now));

        events.publishEvent(new PaymentAccepted(new PaymentInitiated(
                payment.getId(), payment.getEndToEndId(), payment.getDebtorAccount(),
                payment.getCreditorAccount(), payment.getAmount(), payment.getCurrency(),
                payment.getChannel(), payment.getCreditorCountry(), now)));
        return new CreationResult(payment, true);
    }

    @Transactional(readOnly = true)
    public Payment get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new PaymentNotFoundException(id));
    }

    /** Applies a decision from the fraud-service. Safe to call more than once for the same decision. */
    @Transactional
    public void applyDecision(FraudDecision decision) {
        repository.findById(decision.paymentId()).ifPresentOrElse(payment -> {
            boolean applied = payment.applyDecision(decision.decision(), decision.riskScore(), clock.instant());
            if (applied) {
                log.info("Payment {} -> {} (score {})", payment.getId(), payment.getStatus(), decision.riskScore());
            } else {
                log.debug("Ignoring duplicate decision for payment {}", payment.getId());
            }
        }, () -> log.warn("Decision received for unknown payment {}", decision.paymentId()));
    }
}
