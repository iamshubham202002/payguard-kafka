package com.shubham.payguard.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shubham.payguard.common.Channel;
import com.shubham.payguard.common.Decision;
import com.shubham.payguard.common.FraudDecision;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Mock
    PaymentRepository repository;

    @Mock
    ApplicationEventPublisher events;

    PaymentService service;

    @BeforeEach
    void setUp() {
        service = new PaymentService(repository, events, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static PaymentRequest request() {
        return new PaymentRequest("E2E-1", "GB00DEBTOR", "GB00CREDITOR", new BigDecimal("250.00"),
                "GBP", Channel.ONLINE, "GB");
    }

    @Test
    void newPaymentIsSavedAsReceivedAndAnnounced() {
        when(repository.findByEndToEndId("E2E-1")).thenReturn(Optional.empty());
        when(repository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentService.CreationResult result = service.create(request());

        assertThat(result.created()).isTrue();
        assertThat(result.payment().getStatus()).isEqualTo(PaymentStatus.RECEIVED);
        ArgumentCaptor<Object> published = ArgumentCaptor.forClass(Object.class);
        verify(events).publishEvent(published.capture());
        PaymentAccepted accepted = (PaymentAccepted) published.getValue();
        assertThat(accepted.event().endToEndId()).isEqualTo("E2E-1");
        assertThat(accepted.event().debtorAccount()).isEqualTo("GB00DEBTOR");
    }

    @Test
    void duplicateEndToEndIdReturnsExistingPaymentWithoutPublishing() {
        Payment existing = new Payment(UUID.randomUUID(), "E2E-1", "GB00DEBTOR", "GB00CREDITOR",
                new BigDecimal("250.00"), "GBP", Channel.ONLINE, "GB", NOW);
        when(repository.findByEndToEndId("E2E-1")).thenReturn(Optional.of(existing));

        PaymentService.CreationResult result = service.create(request());

        assertThat(result.created()).isFalse();
        assertThat(result.payment()).isSameAs(existing);
        verify(repository, never()).save(any());
        verify(events, never()).publishEvent(any(Object.class));
    }

    @Test
    void rejectDecisionMarksPaymentRejected() {
        Payment payment = new Payment(UUID.randomUUID(), "E2E-1", "GB00DEBTOR", "GB00CREDITOR",
                new BigDecimal("250.00"), "GBP", Channel.ONLINE, "GB", NOW);
        when(repository.findById(payment.getId())).thenReturn(Optional.of(payment));

        service.applyDecision(new FraudDecision(payment.getId(), Decision.REJECT, 90, List.of("X"), NOW));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REJECTED);
        assertThat(payment.getRiskScore()).isEqualTo(90);
    }

    @Test
    void secondDecisionForSamePaymentIsIgnored() {
        Payment payment = new Payment(UUID.randomUUID(), "E2E-1", "GB00DEBTOR", "GB00CREDITOR",
                new BigDecimal("250.00"), "GBP", Channel.ONLINE, "GB", NOW);
        when(repository.findById(payment.getId())).thenReturn(Optional.of(payment));

        service.applyDecision(new FraudDecision(payment.getId(), Decision.APPROVE, 0, List.of(), NOW));
        service.applyDecision(new FraudDecision(payment.getId(), Decision.REJECT, 90, List.of("X"), NOW));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
    }

    @Test
    void unknownPaymentIdThrowsNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(id)).isInstanceOf(PaymentNotFoundException.class);
    }
}
