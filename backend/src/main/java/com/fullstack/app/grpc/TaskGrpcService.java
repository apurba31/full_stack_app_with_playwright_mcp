package com.fullstack.app.grpc;

import com.fullstack.app.domain.TaskPriority;
import com.fullstack.app.domain.TaskStatus;
import com.fullstack.app.dto.CreateTaskRequest;
import com.fullstack.app.dto.TaskDto;
import com.fullstack.app.exception.TaskNotFoundException;
import com.fullstack.app.grpc.proto.GetTaskRequest;
import com.fullstack.app.grpc.proto.ListTasksRequest;
import com.fullstack.app.grpc.proto.ListTasksResponse;
import com.fullstack.app.grpc.proto.Task;
import com.fullstack.app.grpc.proto.TaskServiceGrpc;
import com.fullstack.app.service.TaskService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.UUID;

@GrpcService
public class TaskGrpcService extends TaskServiceGrpc.TaskServiceImplBase {

    private final TaskService taskService;

    public TaskGrpcService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public void getTask(GetTaskRequest request, StreamObserver<Task> responseObserver) {
        try {
            UUID id = UUID.fromString(request.getId());
            TaskDto dto = taskService.get(id);
            responseObserver.onNext(toProto(dto));
            responseObserver.onCompleted();
        } catch (TaskNotFoundException ex) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException());
        } catch (IllegalArgumentException ex) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid id: " + request.getId())
                    .asRuntimeException());
        }
    }

    @Override
    public void listTasks(ListTasksRequest request, StreamObserver<ListTasksResponse> responseObserver) {
        int pageSize = request.getPageSize() <= 0 ? 20 : request.getPageSize();
        int pageNumber = Math.max(request.getPageNumber(), 0);
        TaskStatus statusFilter = mapFromProto(request.getStatus());
        Page<TaskDto> page = taskService.list(
                statusFilter,
                null,
                null,
                PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        ListTasksResponse.Builder builder = ListTasksResponse.newBuilder()
                .setTotal(page.getTotalElements());
        page.getContent().forEach(dto -> builder.addTasks(toProto(dto)));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void createTask(com.fullstack.app.grpc.proto.CreateTaskRequest request,
                           StreamObserver<Task> responseObserver) {
        try {
            TaskPriority priority = mapFromProto(request.getPriority());
            CreateTaskRequest dtoReq = new CreateTaskRequest(
                    request.getTitle(),
                    request.getDescription().isEmpty() ? null : request.getDescription(),
                    priority
            );
            TaskDto created = taskService.create(dtoReq);
            responseObserver.onNext(toProto(created));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException ex) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(ex.getMessage())
                    .asRuntimeException());
        }
    }

    private static Task toProto(TaskDto dto) {
        Task.Builder builder = Task.newBuilder()
                .setId(dto.id() == null ? "" : dto.id().toString())
                .setTitle(dto.title() == null ? "" : dto.title())
                .setDescription(dto.description() == null ? "" : dto.description())
                .setStatus(toProto(dto.status()))
                .setPriority(toProto(dto.priority()))
                .setCreatedAt(dto.createdAt() == null ? "" : dto.createdAt().toString())
                .setUpdatedAt(dto.updatedAt() == null ? "" : dto.updatedAt().toString());
        return builder.build();
    }

    private static com.fullstack.app.grpc.proto.TaskStatus toProto(TaskStatus status) {
        if (status == null) {
            return com.fullstack.app.grpc.proto.TaskStatus.TASK_STATUS_UNSPECIFIED;
        }
        return switch (status) {
            case TODO -> com.fullstack.app.grpc.proto.TaskStatus.TODO;
            case IN_PROGRESS -> com.fullstack.app.grpc.proto.TaskStatus.IN_PROGRESS;
            case DONE -> com.fullstack.app.grpc.proto.TaskStatus.DONE;
        };
    }

    private static com.fullstack.app.grpc.proto.TaskPriority toProto(TaskPriority priority) {
        if (priority == null) {
            return com.fullstack.app.grpc.proto.TaskPriority.TASK_PRIORITY_UNSPECIFIED;
        }
        return switch (priority) {
            case LOW -> com.fullstack.app.grpc.proto.TaskPriority.LOW;
            case MEDIUM -> com.fullstack.app.grpc.proto.TaskPriority.MEDIUM;
            case HIGH -> com.fullstack.app.grpc.proto.TaskPriority.HIGH;
        };
    }

    private static TaskStatus mapFromProto(com.fullstack.app.grpc.proto.TaskStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case TODO -> TaskStatus.TODO;
            case IN_PROGRESS -> TaskStatus.IN_PROGRESS;
            case DONE -> TaskStatus.DONE;
            case TASK_STATUS_UNSPECIFIED, UNRECOGNIZED -> null;
        };
    }

    private static TaskPriority mapFromProto(com.fullstack.app.grpc.proto.TaskPriority priority) {
        if (priority == null) {
            return TaskPriority.MEDIUM;
        }
        return switch (priority) {
            case LOW -> TaskPriority.LOW;
            case MEDIUM -> TaskPriority.MEDIUM;
            case HIGH -> TaskPriority.HIGH;
            case TASK_PRIORITY_UNSPECIFIED, UNRECOGNIZED -> TaskPriority.MEDIUM;
        };
    }
}
