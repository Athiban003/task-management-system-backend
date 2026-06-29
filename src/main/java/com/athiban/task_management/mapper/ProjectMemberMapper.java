package com.athiban.task_management.mapper;

import com.athiban.task_management.dto.ProjectMemberResponse;
import com.athiban.task_management.models.ProjectMember;

public class ProjectMemberMapper {

    private ProjectMemberMapper() {
    }

    public static ProjectMemberResponse toResponse(ProjectMember member) {

        return new ProjectMemberResponse(

                member.getUser().getId(),

                member.getUser().getName(),

                member.getUser().getEmail(),

                member.getRole(),

                member.getAddedAt()
        );
    }
}