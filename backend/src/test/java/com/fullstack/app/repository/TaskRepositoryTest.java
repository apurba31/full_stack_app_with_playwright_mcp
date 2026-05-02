package com.fullstack.app.repository;

import com.fullstack.app.domain.Task;
import com.fullstack.app.domain.TaskPriority;
import com.fullstack.app.domain.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        repository.save(makeTask("First task", TaskStatus.TODO, TaskPriority.LOW));
        repository.save(makeTask("Second task", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM));
        repository.save(makeTask("Important deliverable", TaskStatus.DONE, TaskPriority.HIGH));
    }

    private Task makeTask(String title, TaskStatus status, TaskPriority priority) {
        Task t = new Task();
        t.setTitle(title);
        t.setDescription("desc");
        t.setStatus(status);
        t.setPriority(priority);
        t.setCreatedAt(Instant.now());
        t.setUpdatedAt(Instant.now());
        return t;
    }

    @Test
    void findByStatusReturnsMatching() {
        Page<Task> page = repository.findByStatus(TaskStatus.IN_PROGRESS, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1)
                .extracting(Task::getStatus).containsOnly(TaskStatus.IN_PROGRESS);
    }

    @Test
    void findByPriorityReturnsMatching() {
        Page<Task> page = repository.findByPriority(TaskPriority.HIGH, PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1)
                .extracting(Task::getPriority).containsOnly(TaskPriority.HIGH);
    }

    @Test
    void findByTitleContainingIgnoreCaseReturnsMatching() {
        Page<Task> page = repository.findByTitleContainingIgnoreCase("TASK", PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void specificationFilterCombinesPredicates() {
        Page<Task> page = repository.findAll(
                TaskRepository.withFilters(TaskStatus.TODO, TaskPriority.LOW, "first"),
                PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("First task");
    }

    @Test
    void specificationWithNoFiltersReturnsAll() {
        Page<Task> page = repository.findAll(
                TaskRepository.withFilters(null, null, null),
                PageRequest.of(0, 10));
        assertThat(page.getContent()).hasSize(3);
    }
}
