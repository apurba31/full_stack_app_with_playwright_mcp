package com.fullstack.app.repository;

import com.fullstack.app.domain.Task;
import com.fullstack.app.domain.TaskPriority;
import com.fullstack.app.domain.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByPriority(TaskPriority priority, Pageable pageable);

    Page<Task> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    static Specification<Task> withFilters(TaskStatus status, TaskPriority priority, String titleQuery) {
        return (root, query, cb) -> {
            Specification<Task> spec = Specification.where(null);
            jakarta.persistence.criteria.Predicate predicate = cb.conjunction();
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicate = cb.and(predicate, cb.equal(root.get("priority"), priority));
            }
            if (titleQuery != null && !titleQuery.isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("title")), "%" + titleQuery.toLowerCase() + "%"));
            }
            return predicate;
        };
    }
}
