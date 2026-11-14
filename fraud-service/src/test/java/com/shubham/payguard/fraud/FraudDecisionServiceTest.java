package com.shubham.payguard.fraud;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shubham.payguard.common.Decision;
import com.shubham.payguard.common.FraudDecision;
import com.shubham.payguard.common.PaymentInitiated;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FraudDecisionServiceTest {

    @Mock
    RiskScorer scorer;

    @Mock
    FraudDecisionRepository repository;

    @InjectMocks
    FraudDecisionService service;

    @Test
    void newPaymentIsScoredAndStored() {
        PaymentInitiated payment = TestPayments.payment("20000");
        FraudDecision scored = new FraudDecision(payment.paymentId(), Decision.REVIEW, 40,
                List.of("HIGH_VALUE"), Instant.parse("2026-01-01T00:00:00Z"));
        when(repository.findById(payment.paymentId())).thenReturn(Optional.empty());
        when(scorer.score(payment)).thenReturn(scored);

        FraudDecision result = service.decide(payment);

        assertThat(result).isEqualTo(scored);
        ArgumentCaptor<FraudDecisionEntity> saved = ArgumentCaptor.forClass(FraudDecisionEntity.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getTriggeredRules()).isEqualTo("HIGH_VALUE");
        assertThat(saved.getValue().getDecision()).isEqualTo(Decision.REVIEW);
    }

    @Test
    void redeliveredPaymentReturnsStoredDecisionWithoutRescoring() {
        PaymentInitiated payment = TestPayments.payment("20000");
        FraudDecisionEntity stored = new FraudDecisionEntity(payment.paymentId(), payment.debtorAccount(),
                payment.amount(), Decision.REJECT, 90, "HIGH_VALUE,VELOCITY",
                Instant.parse("2026-01-01T00:00:00Z"));
        when(repository.findById(payment.paymentId())).thenReturn(Optional.of(stored));

        FraudDecision result = service.decide(payment);

        assertThat(result.decision()).isEqualTo(Decision.REJECT);
        assertThat(result.triggeredRules()).containsExactly("HIGH_VALUE", "VELOCITY");
        verify(scorer, never()).score(any());
        verify(repository, never()).save(any());
    }
}
