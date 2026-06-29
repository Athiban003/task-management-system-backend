package com.athiban.task_management.dto;

import com.athiban.task_management.models.TaskStatus;

import java.time.LocalDateTime;

public class TaskResponse {

    private Long id;

    private String title;

    private String description;

    private TaskStatus status;

    private Long assignedToId;

    private String assignedToName;

    private String assignedToEmail;

    private Long createdById;

    private String createdByName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public TaskResponse(
            Long id,
            String title,
            String description,
            TaskStatus status,
            Long assignedToId,
            String assignedToName,
            String assignedToEmail,
            Long createdById,
            String createdByName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.assignedToId = assignedToId;
        this.assignedToName = assignedToName;
        this.assignedToEmail = assignedToEmail;
        this.createdById = createdById;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public String getAssignedToEmail() {
        return assignedToEmail;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}