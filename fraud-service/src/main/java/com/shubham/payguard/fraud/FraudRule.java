package com.shubham.payguard.fraud;

import com.shubham.payguard.common.PaymentInitiated;
import java.util.Optional;

/** One independent fraud check. Add a new rule by implementing this and registering it as a bean. */
public interface FraudRule {

    Optional<RuleHit> evaluate(PaymentInitiated payment);
}
