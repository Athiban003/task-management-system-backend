package com.athiban.task_management.repository;

import com.athiban.task_management.models.Project;
import com.athiban.task_management.models.Task;
import com.athiban.task_management.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // Find tasks in a project
    List<Task> findByProject(Project project);

    Page<Task> findByProject(Project project, Pageable pageable);

    // Find task inside a specific project
    Optional<Task> findByIdAndProject(Long id, Project project);

    // Find tasks assigned to a user
    List<Task> findByAssignedTo(User user);

    // Find tasks created by a user
    List<Task> findByCreatedBy(User user);
}