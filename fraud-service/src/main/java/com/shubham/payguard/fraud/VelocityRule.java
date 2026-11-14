package com.shubham.payguard.fraud;

import com.shubham.payguard.common.PaymentInitiated;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Flags an account that makes more than {@code maxPayments} payments inside a sliding window.
 *
 * <p>State is in memory and per instance. This works because the topic is keyed by debtor account,
 * so one account's payments always reach the same consumer. State is lost on restart; a production
 * version would use a Kafka Streams state store or Redis.
 */
@Component
public class VelocityRule implements FraudRule {

    private final Map<String, Deque<Instant>> recentByAccount = new ConcurrentHashMap<>();
    private final int maxPayments;
    private final Duration window;
    private final Clock clock;

    public VelocityRule(@Value("${fraud.rules.velocity.max-payments:5}") int maxPayments,
                        @Value("${fraud.rules.velocity.window:PT1M}") Duration window,
                        Clock clock) {
        this.maxPayments = maxPayments;
        this.window = window;
        this.clock = clock;
    }

    @Override
    public Optional<RuleHit> evaluate(PaymentInitiated payment) {
        Instant now = clock.instant();
        Instant cutoff = now.minus(window);
        Deque<Instant> timestamps = recentByAccount.computeIfAbsent(payment.debtorAccount(), k -> new ArrayDeque<>());
        int count;
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
                timestamps.pollFirst();
            }
            timestamps.addLast(now);
            count = timestamps.size();
        }
        return count > maxPayments ? Optional.of(new RuleHit("VELOCITY", 40)) : Optional.empty();
    }
}
