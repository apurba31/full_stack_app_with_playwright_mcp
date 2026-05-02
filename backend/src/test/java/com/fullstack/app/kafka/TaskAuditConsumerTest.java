package com.fullstack.app.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TaskAuditConsumerTest.TestApp.class)
@EmbeddedKafka(partitions = 1, topics = {
        KafkaTopics.TASKS_CREATED_V1,
        KafkaTopics.TASKS_UPDATED_V1,
        KafkaTopics.TASKS_COMPLETED_V1
})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
        "spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
        "spring.kafka.consumer.group-id=task-audit",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
        "grpc.server.port=-1"
})
@ActiveProfiles("test")
class TaskAuditConsumerTest {

    @Autowired
    private TaskAuditConsumer consumer;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Test
    void consumesEventsFromAllTopics() {
        long before = consumer.getHandledCount();
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        ProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(
                producerProps, new StringSerializer(), new StringSerializer());
        KafkaTemplate<String, String> template = new KafkaTemplate<>(pf);

        template.send(new ProducerRecord<>(KafkaTopics.TASKS_CREATED_V1, "k1", "{\"id\":\"k1\"}"));
        template.send(new ProducerRecord<>(KafkaTopics.TASKS_UPDATED_V1, "k2", "{\"id\":\"k2\"}"));
        template.send(new ProducerRecord<>(KafkaTopics.TASKS_COMPLETED_V1, "k3", "{\"id\":\"k3\"}"));
        template.flush();

        Awaitility.await().atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> assertThat(consumer.getHandledCount()).isGreaterThanOrEqualTo(before + 3));
    }

    @SpringBootApplication(scanBasePackageClasses = TaskAuditConsumer.class)
    static class TestApp {
    }
}
