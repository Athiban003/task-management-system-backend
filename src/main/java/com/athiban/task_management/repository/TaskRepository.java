package com.athiban.task_management.repository;

import com.athiban.task_management.models.Project;
import com.athiban.task_management.models.Task;
import com.athiban.task_management.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // Find tasks in a project
    List<Task> findByProject(Project project);

    // Find tasks assigned to a user
    List<Task> findByAssignedTo(User user);

    // Find tasks created by a user
    List<Task> findByCreatedBy(User user);
}