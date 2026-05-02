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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository repository;
    private final TaskMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    public TaskService(TaskRepository repository, TaskMapper mapper, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TaskDto create(CreateTaskRequest request) {
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(TaskStatus.TODO);
        task.setPriority(request.priority() == null ? TaskPriority.MEDIUM : request.priority());
        Task saved = repository.save(task);
        TaskDto dto = mapper.toDto(saved);
        eventPublisher.publishEvent(new TaskEvent(TaskEvent.EventType.CREATED, dto));
        return dto;
    }

    @Transactional(readOnly = true)
    public TaskDto get(UUID id) {
        Task task = repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        return mapper.toDto(task);
    }

    @Transactional(readOnly = true)
    public Page<TaskDto> list(TaskStatus status, TaskPriority priority, String query, Pageable pageable) {
        Page<Task> page = repository.findAll(TaskRepository.withFilters(status, priority, query), pageable);
        return page.map(mapper::toDto);
    }

    @Transactional
    public TaskDto update(UUID id, UpdateTaskRequest request) {
        Task task = repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        TaskStatus previousStatus = task.getStatus();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setUpdatedAt(Instant.now());
        Task saved = repository.save(task);
        TaskDto dto = mapper.toDto(saved);
        eventPublisher.publishEvent(new TaskEvent(TaskEvent.EventType.UPDATED, dto));
        if (previousStatus != TaskStatus.DONE && saved.getStatus() == TaskStatus.DONE) {
            eventPublisher.publishEvent(new TaskEvent(TaskEvent.EventType.COMPLETED, dto));
        }
        return dto;
    }

    @Transactional
    public TaskDto patchStatus(UUID id, UpdateStatusRequest request) {
        Task task = repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        TaskStatus previousStatus = task.getStatus();
        task.setStatus(request.status());
        task.setUpdatedAt(Instant.now());
        Task saved = repository.save(task);
        TaskDto dto = mapper.toDto(saved);
        eventPublisher.publishEvent(new TaskEvent(TaskEvent.EventType.UPDATED, dto));
        if (previousStatus != TaskStatus.DONE && saved.getStatus() == TaskStatus.DONE) {
            eventPublisher.publishEvent(new TaskEvent(TaskEvent.EventType.COMPLETED, dto));
        }
        return dto;
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
