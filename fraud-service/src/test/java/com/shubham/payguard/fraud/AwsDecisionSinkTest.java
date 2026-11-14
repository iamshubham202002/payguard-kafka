package com.shubham.payguard.fraud;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shubham.payguard.common.Decision;
import com.shubham.payguard.common.FraudDecision;
import com.shubham.payguard.common.PaymentInitiated;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@ExtendWith(MockitoExtension.class)
class AwsDecisionSinkTest {

    private static final Instant DECIDED_AT = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    S3Client s3;

    @Mock
    SqsClient sqs;

    AwsDecisionSink sink;

    @BeforeEach
    void setUp() {
        sink = new AwsDecisionSink(s3, sqs, new ObjectMapper().findAndRegisterModules(), "archive-bucket", "review-queue");
    }

    private static FraudDecision decision(PaymentInitiated payment, Decision decision, int score) {
        return new FraudDecision(payment.paymentId(), decision, score, List.of("HIGH_VALUE"), DECIDED_AT);
    }

    @Test
    void approvedPaymentIsArchivedButNotQueued() {
        PaymentInitiated payment = TestPayments.payment("100");

        sink.publish(payment, decision(payment, Decision.APPROVE, 0));

        ArgumentCaptor<PutObjectRequest> put = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3).putObject(put.capture(), any(RequestBody.class));
        assertThat(put.getValue().bucket()).isEqualTo("archive-bucket");
        assertThat(put.getValue().key()).isEqualTo("decisions/2026-01-01/" + payment.paymentId() + ".json");
        verifyNoInteractions(sqs);
    }

    @Test
    void reviewPaymentIsArchivedAndQueuedForAnalysts() {
        PaymentInitiated payment = TestPayments.payment("20000");
        when(sqs.getQueueUrl(any(GetQueueUrlRequest.class)))
                .thenReturn(GetQueueUrlResponse.builder().queueUrl("http://sqs/review-queue").build());

        sink.publish(payment, decision(payment, Decision.REVIEW, 40));

        verify(s3).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        ArgumentCaptor<SendMessageRequest> sent = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqs).sendMessage(sent.capture());
        assertThat(sent.getValue().queueUrl()).isEqualTo("http://sqs/review-queue");
        assertThat(sent.getValue().messageBody()).contains(payment.paymentId().toString());
        assertThat(sent.getValue().messageAttributes().get("decision").stringValue()).isEqualTo("REVIEW");
    }
}
