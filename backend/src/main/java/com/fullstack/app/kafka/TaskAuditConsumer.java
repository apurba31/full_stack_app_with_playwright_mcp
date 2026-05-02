package com.fullstack.app.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class TaskAuditConsumer {

    private static final Logger log = LoggerFactory.getLogger(TaskAuditConsumer.class);

    private final AtomicLong handledCount = new AtomicLong();

    @KafkaListener(
            topics = {
                    KafkaTopics.TASKS_CREATED_V1,
                    KafkaTopics.TASKS_UPDATED_V1,
                    KafkaTopics.TASKS_COMPLETED_V1
            },
            groupId = KafkaTopics.AUDIT_GROUP
    )
    public void onTaskEvent(@Payload String payload,
                            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                            @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) String key) {
        handledCount.incrementAndGet();
        log.info("[AUDIT] topic={} key={} payload={}", topic, key, payload);
    }

    public long getHandledCount() {
        return handledCount.get();
    }
}
