package com.fullstack.app.grpc;

import com.fullstack.app.grpc.proto.CreateTaskRequest;
import com.fullstack.app.grpc.proto.GetTaskRequest;
import com.fullstack.app.grpc.proto.ListTasksRequest;
import com.fullstack.app.grpc.proto.ListTasksResponse;
import com.fullstack.app.grpc.proto.Task;
import com.fullstack.app.grpc.proto.TaskPriority;
import com.fullstack.app.grpc.proto.TaskServiceGrpc;
import com.fullstack.app.grpc.proto.TaskStatus;
import com.fullstack.app.kafka.KafkaTopics;
import com.fullstack.app.repository.TaskRepository;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = com.fullstack.app.BackendApplication.class)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {
        KafkaTopics.TASKS_CREATED_V1,
        KafkaTopics.TASKS_UPDATED_V1,
        KafkaTopics.TASKS_COMPLETED_V1
})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
class TaskGrpcServiceTest {

    @Autowired
    private TaskGrpcService grpcService;

    @Autowired
    private TaskRepository repository;

    private Server server;
    private ManagedChannel channel;
    private TaskServiceGrpc.TaskServiceBlockingStub stub;

    @BeforeEach
    void setUp() throws Exception {
        repository.deleteAll();
        String name = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(name)
                .directExecutor()
                .addService(grpcService)
                .build()
                .start();
        channel = InProcessChannelBuilder.forName(name).directExecutor().build();
        stub = TaskServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() {
        if (channel != null) channel.shutdownNow();
        if (server != null) server.shutdownNow();
    }

    @Test
    void createTaskReturnsCreatedTask() {
        Task created = stub.createTask(CreateTaskRequest.newBuilder()
                .setTitle("Hello")
                .setDescription("World")
                .setPriority(TaskPriority.HIGH)
                .build());

        assertThat(created.getId()).isNotEmpty();
        assertThat(created.getTitle()).isEqualTo("Hello");
        assertThat(created.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(created.getPriority()).isEqualTo(TaskPriority.HIGH);
    }

    @Test
    void getTaskRoundTrips() {
        Task created = stub.createTask(CreateTaskRequest.newBuilder()
                .setTitle("Roundtrip")
                .setPriority(TaskPriority.MEDIUM)
                .build());

        Task fetched = stub.getTask(GetTaskRequest.newBuilder().setId(created.getId()).build());
        assertThat(fetched.getId()).isEqualTo(created.getId());
        assertThat(fetched.getTitle()).isEqualTo("Roundtrip");
    }

    @Test
    void getTaskUnknownIdReturnsNotFound() {
        assertThatThrownBy(() -> stub.getTask(GetTaskRequest.newBuilder()
                .setId("00000000-0000-0000-0000-000000000000").build()))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("NOT_FOUND");
    }

    @Test
    void getTaskInvalidIdReturnsInvalidArgument() {
        assertThatThrownBy(() -> stub.getTask(GetTaskRequest.newBuilder().setId("not-a-uuid").build()))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("INVALID_ARGUMENT");
    }

    @Test
    void listTasksHonoursFilters() {
        stub.createTask(CreateTaskRequest.newBuilder().setTitle("a").setPriority(TaskPriority.LOW).build());
        stub.createTask(CreateTaskRequest.newBuilder().setTitle("b").setPriority(TaskPriority.LOW).build());

        ListTasksResponse all = stub.listTasks(ListTasksRequest.newBuilder()
                .setPageSize(10).setPageNumber(0).build());
        assertThat(all.getTotal()).isEqualTo(2);
        assertThat(all.getTasksList()).hasSize(2);

        ListTasksResponse byStatus = stub.listTasks(ListTasksRequest.newBuilder()
                .setStatus(TaskStatus.DONE).setPageSize(10).build());
        assertThat(byStatus.getTotal()).isEqualTo(0);
    }
}
