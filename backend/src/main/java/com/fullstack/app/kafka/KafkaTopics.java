package com.fullstack.app.kafka;

public final class KafkaTopics {

    public static final String TASKS_CREATED_V1 = "tasks.created.v1";
    public static final String TASKS_UPDATED_V1 = "tasks.updated.v1";
    public static final String TASKS_COMPLETED_V1 = "tasks.completed.v1";

    public static final String AUDIT_GROUP = "task-audit";

    private KafkaTopics() {
    }
}
