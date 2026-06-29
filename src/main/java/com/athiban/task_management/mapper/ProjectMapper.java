package com.athiban.task_management.mapper;

import com.athiban.task_management.dto.ProjectResponse;
import com.athiban.task_management.models.Project;

public class ProjectMapper {

    private ProjectMapper() {
    }

    public static ProjectResponse toResponse(Project project) {

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getDeadline(),
                project.getStatus(),

                project.getCreatedBy().getId(),
                project.getCreatedBy().getName(),
                project.getCreatedBy().getEmail(),

                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}