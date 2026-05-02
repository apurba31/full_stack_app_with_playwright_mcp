package com.fullstack.app.service;

import com.fullstack.app.domain.Task;
import com.fullstack.app.domain.TaskPriority;
import com.fullstack.app.domain.TaskStatus;
import com.fullstack.app.dto.CreateTaskRequest;
import com.fullstack.app.dto.TaskDto;
import com.fullstack.app.dto.UpdateStatusRequest;
import com.fullstack.app.dto.UpdateTaskRequest;
import com.fullstack.app.exception.TaskNotFoundException;
import com.fullstack.app.kafka.TaskEvent;
import com.fullstack.app.mapper.TaskMapper;
import com.fullstack.app.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository repository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private TaskMapper mapper;

    private TaskService service;

    @BeforeEach
    void setUp() {
        mapper = new TaskMapper();
        service = new TaskService(repository, mapper, eventPublisher);
    }

    private Task makeTask(UUID id, TaskStatus status) {
        Task t = new Task();
        t.setId(id);
        t.setTitle("Title");
        t.setDescription("Desc");
        t.setStatus(status);
        t.setPriority(TaskPriority.MEDIUM);
        t.setCreatedAt(Instant.now());
        t.setUpdatedAt(Instant.now());
        return t;
    }

    @Test
    void createPersistsTaskAndPublishesCreatedEvent() {
        UUID id = UUID.randomUUID();
        when(repository.save(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            t.setId(id);
            t.setCreatedAt(Instant.now());
            t.setUpdatedAt(Instant.now());
            return t;
        });

        TaskDto dto = service.create(new CreateTaskRequest("T", "D", TaskPriority.HIGH));

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.status()).isEqualTo(TaskStatus.TODO);
        assertThat(dto.priority()).isEqualTo(TaskPriority.HIGH);

        ArgumentCaptor<TaskEvent> captor = ArgumentCaptor.forClass(TaskEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(TaskEvent.EventType.CREATED);
    }

    @Test
    void createDefaultsPriorityToMediumWhenNull() {
        when(repository.save(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            t.setId(UUID.randomUUID());
            t.setCreatedAt(Instant.now());
            t.setUpdatedAt(Instant.now());
            return t;
        });

        TaskDto dto = service.create(new CreateTaskRequest("T", null, null));

        assertThat(dto.priority()).isEqualTo(TaskPriority.MEDIUM);
    }

    @Test
    void getReturnsTaskWhenFound() {
        UUID id = UUID.randomUUID();
        Task task = makeTask(id, TaskStatus.TODO);
        when(repository.findById(id)).thenReturn(Optional.of(task));

        TaskDto dto = service.get(id);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.title()).isEqualTo("Title");
    }

    @Test
    void getThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(id))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void listAppliesFiltersAndMapsResults() {
        Task task = makeTask(UUID.randomUUID(), TaskStatus.IN_PROGRESS);
        Page<Task> page = new PageImpl<>(List.of(task));
        when(repository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        Page<TaskDto> result = service.list(TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, "ti", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).status()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void updatePublishesUpdatedAndCompletedWhenStatusBecomesDone() {
        UUID id = UUID.randomUUID();
        Task existing = makeTask(id, TaskStatus.IN_PROGRESS);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateTaskRequest req = new UpdateTaskRequest("New", "NewDesc", TaskStatus.DONE, TaskPriority.HIGH);
        TaskDto dto = service.update(id, req);

        assertThat(dto.status()).isEqualTo(TaskStatus.DONE);

        ArgumentCaptor<TaskEvent> captor = ArgumentCaptor.forClass(TaskEvent.class);
        verify(eventPublisher, times(2)).publishEvent(captor.capture());
        List<TaskEvent.EventType> types = captor.getAllValues().stream().map(TaskEvent::type).toList();
        assertThat(types).containsExactly(TaskEvent.EventType.UPDATED, TaskEvent.EventType.COMPLETED);
    }

    @Test
    void updatePublishesOnlyUpdatedWhenStatusUnchanged() {
        UUID id = UUID.randomUUID();
        Task existing = makeTask(id, TaskStatus.IN_PROGRESS);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateTaskRequest req = new UpdateTaskRequest("New", "NewDesc", TaskStatus.IN_PROGRESS, TaskPriority.HIGH);
        service.update(id, req);

        ArgumentCaptor<TaskEvent> captor = ArgumentCaptor.forClass(TaskEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(TaskEvent.EventType.UPDATED);
    }

    @Test
    void updateThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id,
                new UpdateTaskRequest("T", "D", TaskStatus.TODO, TaskPriority.LOW)))
                .isInstanceOf(TaskNotFoundException.class);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void patchStatusPublishesCompletedWhenTransitioningToDone() {
        UUID id = UUID.randomUUID();
        Task existing = makeTask(id, TaskStatus.TODO);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        service.patchStatus(id, new UpdateStatusRequest(TaskStatus.DONE));

        ArgumentCaptor<TaskEvent> captor = ArgumentCaptor.forClass(TaskEvent.class);
        verify(eventPublisher, times(2)).publishEvent(captor.capture());
        List<TaskEvent.EventType> types = captor.getAllValues().stream().map(TaskEvent::type).toList();
        assertThat(types).containsExactly(TaskEvent.EventType.UPDATED, TaskEvent.EventType.COMPLETED);
    }

    @Test
    void patchStatusDoesNotRePublishCompletedIfAlreadyDone() {
        UUID id = UUID.randomUUID();
        Task existing = makeTask(id, TaskStatus.DONE);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        service.patchStatus(id, new UpdateStatusRequest(TaskStatus.DONE));

        verify(eventPublisher, times(1)).publishEvent(any(TaskEvent.class));
    }

    @Test
    void deleteRemovesTaskWhenExists() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        service.delete(id);

        verify(repository).deleteById(id);
    }

    @Test
    void deleteThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(id)).isInstanceOf(TaskNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }
}
