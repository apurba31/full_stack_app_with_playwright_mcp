package com.fullstack.app.config;

import com.fullstack.app.kafka.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    @Bean
    public NewTopic tasksCreatedTopic() {
        return TopicBuilder.name(KafkaTopics.TASKS_CREATED_V1).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic tasksUpdatedTopic() {
        return TopicBuilder.name(KafkaTopics.TASKS_UPDATED_V1).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic tasksCompletedTopic() {
        return TopicBuilder.name(KafkaTopics.TASKS_COMPLETED_V1).partitions(1).replicas(1).build();
    }
}
