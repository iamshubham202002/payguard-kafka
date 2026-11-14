package com.shubham.payguard.fraud;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shubham.payguard.common.Decision;
import com.shubham.payguard.common.FraudDecision;
import com.shubham.payguard.common.PaymentInitiated;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

/**
 * Every decision is archived to S3 as JSON (audit trail). Payments that need a human
 * (REVIEW or REJECT) are also put on an SQS queue for analysts.
 *
 * <p>Both calls are idempotent enough for Kafka's at-least-once delivery: the S3 key is derived from
 * the payment id, so a retry overwrites the same object. SQS standard queues are at-least-once too,
 * so a consumer of the review queue must tolerate duplicates.
 */
@Component
@ConditionalOnProperty(name = "aws.enabled", havingValue = "true")
public class AwsDecisionSink implements DecisionSink {

    private static final Logger log = LoggerFactory.getLogger(AwsDecisionSink.class);

    private final S3Client s3;
    private final SqsClient sqs;
    private final ObjectMapper mapper;
    private final String bucket;
    private final String queueName;
    private volatile String queueUrl;

    public AwsDecisionSink(S3Client s3, SqsClient sqs, ObjectMapper mapper,
                           @Value("${aws.s3.bucket}") String bucket,
                           @Value("${aws.sqs.queue-name}") String queueName) {
        this.s3 = s3;
        this.sqs = sqs;
        this.mapper = mapper;
        this.bucket = bucket;
        this.queueName = queueName;
    }

    /** What gets stored and queued: the original payment plus the verdict. */
    record DecisionRecord(PaymentInitiated payment, FraudDecision decision) {
    }

    @Override
    public void publish(PaymentInitiated payment, FraudDecision decision) {
        String json = toJson(new DecisionRecord(payment, decision));
        archive(decision, json);
        if (decision.decision() != Decision.APPROVE) {
            enqueueForReview(decision, json);
        }
    }

    private void archive(FraudDecision decision, String json) {
        String key = "decisions/%s/%s.json".formatted(
                LocalDate.ofInstant(decision.decidedAt(), ZoneOffset.UTC), decision.paymentId());
        s3.putObject(PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("application/json")
                .build(), RequestBody.fromString(json));
        log.info("Archived decision for payment {} to s3://{}/{}", decision.paymentId(), bucket, key);
    }

    private void enqueueForReview(FraudDecision decision, String json) {
        sqs.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrl())
                .messageBody(json)
                .messageAttributes(java.util.Map.of("decision", MessageAttributeValue.builder()
                        .dataType("String").stringValue(decision.decision().name()).build()))
                .build());
        log.info("Queued payment {} ({}) for analyst review", decision.paymentId(), decision.decision());
    }

    private String queueUrl() {
        String url = queueUrl;
        if (url == null) {
            url = sqs.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build()).queueUrl();
            queueUrl = url;
        }
        return url;
    }

    private String toJson(DecisionRecord record) {
        try {
            return mapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialise decision " + record.decision().paymentId(), e);
        }
    }
}
