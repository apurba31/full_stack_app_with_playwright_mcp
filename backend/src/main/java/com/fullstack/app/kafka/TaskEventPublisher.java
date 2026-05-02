package com.fullstack.app.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fullstack.app.dto.TaskDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TaskEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TaskEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public TaskEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @EventListener
    public void handle(TaskEvent event) {
        TaskDto task = event.task();
        String key = task.id() == null ? null : task.id().toString();
        String payload;
        try {
            payload = objectMapper.writeValueAsString(task);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize task event payload for {}", key, e);
            return;
        }

        switch (event.type()) {
            case CREATED -> send(KafkaTopics.TASKS_CREATED_V1, key, payload);
            case UPDATED -> send(KafkaTopics.TASKS_UPDATED_V1, key, payload);
            case COMPLETED -> send(KafkaTopics.TASKS_COMPLETED_V1, key, payload);
        }
    }

    private void send(String topic, String key, String payload) {
        kafkaTemplate.send(topic, key, payload).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event to topic {} for key {}", topic, key, ex);
            } else {
                log.debug("Published event to {} key={} offset={}",
                        topic, key, result.getRecordMetadata().offset());
            }
        });
    }
}
