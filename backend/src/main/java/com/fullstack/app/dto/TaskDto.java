package com.fullstack.app.dto;

import com.fullstack.app.domain.TaskPriority;
import com.fullstack.app.domain.TaskStatus;

import java.time.Instant;
import java.util.UUID;

public record TaskDto(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        Instant createdAt,
        Instant updatedAt
) {
}
