package com.athiban.task_management.dto;

import com.athiban.task_management.models.ProjectStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProjectResponse {

    private Long id;

    private String name;

    private String description;

    private LocalDate deadline;

    private ProjectStatus status;

    private Long ownerId;

    private String ownerName;

    private String ownerEmail;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public ProjectResponse(
            Long id,
            String name,
            String description,
            LocalDate deadline,
            ProjectStatus status,
            Long ownerId,
            String ownerName,
            String ownerEmail,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.deadline = deadline;
        this.status = status;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.ownerEmail = ownerEmail;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}