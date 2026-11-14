package com.shubham.payguard.fraud;

import com.shubham.payguard.common.Topics;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
public class KafkaConfig {

    @Bean
    NewTopic paymentInitiatedTopic() {
        return TopicBuilder.name(Topics.PAYMENT_INITIATED).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic paymentDecisionsTopic() {
        return TopicBuilder.name(Topics.PAYMENT_DECISIONS).partitions(3).replicas(1).build();
    }

    /** Dead-letter topic for payments that keep failing; same partition count as the source topic. */
    @Bean
    NewTopic paymentInitiatedDeadLetterTopic() {
        return TopicBuilder.name(Topics.PAYMENT_INITIATED + ".DLT").partitions(3).replicas(1).build();
    }

    /**
     * Retries a failing record 4 times with exponential backoff, then publishes it to
     * {@code payments.initiated.DLT} and moves on so one bad message cannot block the partition.
     * Undeserialisable (poison) records skip the retries and go straight to the DLT.
     */
    @Bean
    DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> jsonTemplate, KafkaProperties properties) {
        // Records that failed deserialisation are still raw bytes, so they need a byte[] producer.
        Map<String, Object> config = properties.buildProducerProperties(null);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        KafkaTemplate<String, byte[]> bytesTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(config));

        Map<Class<?>, KafkaOperations<?, ?>> templates = new LinkedHashMap<>();
        templates.put(byte[].class, bytesTemplate);
        templates.put(Object.class, jsonTemplate);

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(4);
        backOff.setInitialInterval(500);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(5_000);
        return new DefaultErrorHandler(new DeadLetterPublishingRecoverer(templates), backOff);
    }
}
