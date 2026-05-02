package com.fullstack.app.kafka;

import com.fullstack.app.dto.TaskDto;

public record TaskEvent(EventType type, TaskDto task) {
    public enum EventType {
        CREATED, UPDATED, COMPLETED
    }
}
