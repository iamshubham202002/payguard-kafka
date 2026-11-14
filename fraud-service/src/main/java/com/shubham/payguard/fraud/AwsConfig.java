package com.shubham.payguard.fraud;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

/**
 * AWS SDK v2 clients. Credentials come from the default provider chain (env vars, profile, IAM role).
 * Set {@code aws.endpoint} to point at LocalStack for local runs; leave it empty for real AWS.
 */
@Configuration
@ConditionalOnProperty(name = "aws.enabled", havingValue = "true")
public class AwsConfig {

    @Bean
    S3Client s3Client(@Value("${aws.region}") String region, @Value("${aws.endpoint:}") String endpoint) {
        S3ClientBuilder builder = S3Client.builder().region(Region.of(region));
        if (!endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint)).forcePathStyle(true);
        }
        return builder.build();
    }

    @Bean
    SqsClient sqsClient(@Value("${aws.region}") String region, @Value("${aws.endpoint:}") String endpoint) {
        SqsClientBuilder builder = SqsClient.builder().region(Region.of(region));
        if (!endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        return builder.build();
    }
}
