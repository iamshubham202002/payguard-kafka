package com.shubham.payguard.payment;

import com.shubham.payguard.common.PaymentInitiated;

/** Spring application event raised inside the DB transaction; published to Kafka only after commit. */
public record PaymentAccepted(PaymentInitiated event) {
}
