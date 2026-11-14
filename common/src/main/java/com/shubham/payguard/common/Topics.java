package com.shubham.payguard.common;

/** Kafka topic names shared by producers and consumers. */
public final class Topics {

    /** Payments accepted by the payment-service, awaiting fraud screening. Keyed by debtor account. */
    public static final String PAYMENT_INITIATED = "payments.initiated";

    /** Fraud decisions published by the fraud-service. Keyed by payment id. */
    public static final String PAYMENT_DECISIONS = "payments.decisions";

    private Topics() {
    }
}
