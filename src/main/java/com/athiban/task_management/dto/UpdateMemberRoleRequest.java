package com.athiban.task_management.dto;

import com.athiban.task_management.models.ProjectMemberRole;
import jakarta.validation.constraints.NotNull;

public class UpdateMemberRoleRequest {

    @NotNull(message = "Role is required")
    private ProjectMemberRole role;

    public ProjectMemberRole getRole() { return role; }
    public void setRole(ProjectMemberRole role) { this.role = role; }
}