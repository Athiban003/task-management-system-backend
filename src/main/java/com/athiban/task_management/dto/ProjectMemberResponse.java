package com.athiban.task_management.dto;

import com.athiban.task_management.models.ProjectMemberRole;

import java.time.LocalDateTime;

public class ProjectMemberResponse {

    private Long userId;

    private String name;

    private String email;

    private ProjectMemberRole role;

    private LocalDateTime addedAt;

    public ProjectMemberResponse(
            Long userId,
            String name,
            String email,
            ProjectMemberRole role,
            LocalDateTime addedAt
    ) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.addedAt = addedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public ProjectMemberRole getRole() {
        return role;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }
}