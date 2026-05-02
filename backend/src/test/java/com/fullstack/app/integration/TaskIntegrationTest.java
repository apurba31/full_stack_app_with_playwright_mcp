package com.fullstack.app.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fullstack.app.domain.TaskPriority;
import com.fullstack.app.kafka.KafkaTopics;
import com.fullstack.app.repository.TaskRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, topics = {
        KafkaTopics.TASKS_CREATED_V1,
        KafkaTopics.TASKS_UPDATED_V1,
        KafkaTopics.TASKS_COMPLETED_V1
})
@ActiveProfiles("test")
class TaskIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TaskRepository repository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", () -> System.getProperty("spring.embedded.kafka.brokers", "localhost:9092"));
    }

    @BeforeAll
    static void initOnce() {
    }

    @AfterAll
    static void cleanup() {
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void crudFlowWorksAndPublishesKafkaEvents() {
        repository.deleteAll();

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("integration-test-" + UUID.randomUUID(),
                "false", embeddedKafka);
        consumerProps.put("key.deserializer", StringDeserializer.class);
        consumerProps.put("value.deserializer", StringDeserializer.class);
        try (Consumer<String, String> consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(java.util.List.of(KafkaTopics.TASKS_CREATED_V1, KafkaTopics.TASKS_COMPLETED_V1));

            // create
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String createBody = "{\"title\":\"My Task\",\"description\":\"Important\",\"priority\":\"HIGH\"}";
            ResponseEntity<JsonNode> createResp = restTemplate.exchange(url("/api/v1/tasks"),
                    HttpMethod.POST, new HttpEntity<>(createBody, headers), JsonNode.class);
            assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            JsonNode body = createResp.getBody();
            assertThat(body).isNotNull();
            UUID id = UUID.fromString(body.get("id").asText());
            assertThat(repository.findById(id)).isPresent();

            // get
            ResponseEntity<JsonNode> getResp = restTemplate.getForEntity(url("/api/v1/tasks/" + id), JsonNode.class);
            assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(getResp.getBody().get("title").asText()).isEqualTo("My Task");

            // update -> mark DONE which should publish completed
            String updateBody = "{\"title\":\"My Task\",\"description\":\"Updated\",\"status\":\"DONE\",\"priority\":\"HIGH\"}";
            ResponseEntity<JsonNode> updResp = restTemplate.exchange(url("/api/v1/tasks/" + id),
                    HttpMethod.PUT, new HttpEntity<>(updateBody, headers), JsonNode.class);
            assertThat(updResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(updResp.getBody().get("status").asText()).isEqualTo("DONE");
            assertThat(repository.findById(id)).isPresent();
            assertThat(repository.findById(id).get().getPriority()).isEqualTo(TaskPriority.HIGH);

            // verify created and completed events
            AtomicReference<Boolean> sawCreated = new AtomicReference<>(false);
            AtomicReference<Boolean> sawCompleted = new AtomicReference<>(false);
            Awaitility.await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> r : records) {
                    if (r.topic().equals(KafkaTopics.TASKS_CREATED_V1) && r.value().contains(id.toString())) {
                        sawCreated.set(true);
                    } else if (r.topic().equals(KafkaTopics.TASKS_COMPLETED_V1) && r.value().contains(id.toString())) {
                        sawCompleted.set(true);
                    }
                }
                assertThat(sawCreated.get()).isTrue();
                assertThat(sawCompleted.get()).isTrue();
            });

            // list
            ResponseEntity<JsonNode> listResp = restTemplate.getForEntity(url("/api/v1/tasks?status=DONE"), JsonNode.class);
            assertThat(listResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(listResp.getBody().get("content").size()).isGreaterThanOrEqualTo(1);

            // delete
            ResponseEntity<Void> delResp = restTemplate.exchange(url("/api/v1/tasks/" + id),
                    HttpMethod.DELETE, null, Void.class);
            assertThat(delResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(repository.findById(id)).isEmpty();
        }
    }

    @Test
    void getReturns404WhenMissing() {
        UUID id = UUID.randomUUID();
        ResponseEntity<JsonNode> resp = restTemplate.getForEntity(url("/api/v1/tasks/" + id), JsonNode.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createReturns400WhenTitleBlank() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"title\":\"\",\"description\":\"D\",\"priority\":\"LOW\"}";
        ResponseEntity<JsonNode> resp = restTemplate.exchange(url("/api/v1/tasks"),
                HttpMethod.POST, new HttpEntity<>(body, headers), JsonNode.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
