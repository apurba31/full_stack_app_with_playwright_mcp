package com.fullstack.app.dto;

import com.fullstack.app.domain.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull(message = "status must not be null")
        TaskStatus status
) {
}
