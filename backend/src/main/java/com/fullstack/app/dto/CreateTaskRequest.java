package com.fullstack.app.dto;

import com.fullstack.app.domain.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @NotBlank(message = "title must not be blank")
        @Size(min = 1, max = 200, message = "title length must be between 1 and 200")
        String title,

        @Size(max = 2000, message = "description length must be at most 2000")
        String description,

        TaskPriority priority
) {
}
