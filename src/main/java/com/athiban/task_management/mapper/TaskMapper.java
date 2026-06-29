package com.athiban.task_management.mapper;

import com.athiban.task_management.dto.TaskResponse;
import com.athiban.task_management.models.Task;

public class TaskMapper {

    private TaskMapper() {
    }

    public static TaskResponse toResponse(Task task) {

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),

                task.getAssignedTo() != null
                        ? task.getAssignedTo().getId()
                        : null,

                task.getAssignedTo() != null
                        ? task.getAssignedTo().getName()
                        : null,

                task.getAssignedTo() != null
                        ? task.getAssignedTo().getEmail()
                        : null,

                task.getCreatedBy().getId(),
                task.getCreatedBy().getName(),

                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}