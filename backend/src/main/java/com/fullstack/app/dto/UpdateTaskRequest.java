package com.fullstack.app.dto;

import com.fullstack.app.domain.TaskPriority;
import com.fullstack.app.domain.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTaskRequest(
        @NotBlank(message = "title must not be blank")
        @Size(min = 1, max = 200, message = "title length must be between 1 and 200")
        String title,

        @Size(max = 2000, message = "description length must be at most 2000")
        String description,

        @NotNull(message = "status must not be null")
        TaskStatus status,

        @NotNull(message = "priority must not be null")
        TaskPriority priority
) {
}
