package com.shubham.payguard.payment;

import com.shubham.payguard.common.Topics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    NewTopic paymentInitiatedTopic() {
        return TopicBuilder.name(Topics.PAYMENT_INITIATED).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic paymentDecisionsTopic() {
        return TopicBuilder.name(Topics.PAYMENT_DECISIONS).partitions(3).replicas(1).build();
    }
}
