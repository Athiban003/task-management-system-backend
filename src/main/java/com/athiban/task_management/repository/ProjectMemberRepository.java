package com.athiban.task_management.repository;

import com.athiban.task_management.models.Project;
import com.athiban.task_management.models.ProjectMember;
import com.athiban.task_management.models.ProjectMemberRole;
import com.athiban.task_management.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    // Find a specific member
    Optional<ProjectMember> findByProjectAndUser(Project project, User user);

    // Check if user in project
    boolean existsByProjectAndUser(Project project, User user);

    // Check if user has specific role in project
    boolean existsByProjectAndUserAndRole(Project project, User user, ProjectMemberRole role);

    // Check if user has any of these roles in project
    boolean existsByProjectAndUserAndRoleIn(Project project, User user, List<ProjectMemberRole> roles);

    // Get all members of a project
    List<ProjectMember> findByProject(Project project);

    Page<ProjectMember> findByProject(
            Project project,
            Pageable pageable
    );

    Page<ProjectMember> findByUser(User user, Pageable pageable);

    // Delete all members when project is deleted
    void deleteByProject(Project project);
}