package com.athiban.task_management.dto;

import com.athiban.task_management.models.ProjectStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateProjectStatusRequest {
    @NotNull
    private ProjectStatus status;

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }


}
