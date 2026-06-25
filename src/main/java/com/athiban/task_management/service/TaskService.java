package com.athiban.task_management.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.athiban.task_management.dto.CreateTaskRequest;
import com.athiban.task_management.dto.UpdateTaskRequest;
import com.athiban.task_management.dto.UpdateTaskStatusRequest;
import com.athiban.task_management.exception.ProjectNotFoundException;
import com.athiban.task_management.models.*;
import com.athiban.task_management.repository.*;
import com.athiban.task_management.security.AuthorizationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuthService authService;
    private final AuthorizationService authorizationService;
    private final ProjectMemberRepository projectMemberRepository;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       UserRepository userRepository,
                       AuditLogRepository auditLogRepository,
                       AuthService authService,
                       AuthorizationService authorizationService,
                       ProjectMemberRepository projectMemberRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.authService = authService;
        this.authorizationService = authorizationService;
        this.projectMemberRepository=projectMemberRepository;
    }

    @Transactional
    public void createTask(Long projectId, CreateTaskRequest request) {
        logger.info("Creating task: {} in project: {}", request.getTitle(), projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));

        User currentUser = authService.getCurrentUser();
        authorizationService.checkCanCreateTask(currentUser, project);

        Task task = new Task(project, currentUser, request.getTitle(), request.getDescription());

        // Assign to user if specified
        if (request.getAssignedToId() != null) {
            User assignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            projectMemberRepository.findByProjectAndUser(project, assignee)
                    .orElseThrow(() -> new IllegalArgumentException("User is not a member of this project"));
            task.setAssignedTo(assignee);
        }

        taskRepository.save(task);

        auditLogRepository.save(
                new AuditLog(
                        AuditAction.TASK_CREATED,
                        task.getId(),
                        "TASK",
                        currentUser.getId(),
                        "Task created: " + task.getTitle()
                )
        );
        logger.info("Task {} created successfully", task.getId());
    }

    @Transactional
    public void updateTask(Long taskId, UpdateTaskRequest request) {
        logger.info("Updating task: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        User currentUser = authService.getCurrentUser();

        authorizationService.checkCanModifyTask(
                currentUser,
                task
        );

        List<String> changes = task.updateDetails(
                request.getTitle(),
                request.getDescription()
        );

        if (!changes.isEmpty()) {
            taskRepository.save(task);

            String details = String.join(", ", changes);

            auditLogRepository.save(
                    new AuditLog(
                            AuditAction.TASK_UPDATED,
                            task.getId(),
                            "TASK",
                            currentUser.getId(),
                            details
                    )
            );
        }
        logger.info("Task {} updated successfully", task.getId());
    }

    @Transactional
    public void updateTaskStatus(Long taskId, UpdateTaskStatusRequest request) {
        logger.info("Updating task status: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        User currentUser = authService.getCurrentUser();
        authorizationService.checkCanUpdateTaskStatus(currentUser, task);

        TaskStatus oldStatus = task.getStatus();
        boolean changed = task.changeStatus(request.getStatus());

        if (changed) {
            taskRepository.save(task);

            auditLogRepository.save(
                    new AuditLog(
                            AuditAction.TASK_STATUS_UPDATED,
                            task.getId(),
                            "TASK",
                            currentUser.getId(),
                            "Status: " + oldStatus + " → " + task.getStatus()
                    )
            );
        }
        logger.info("Status of Task {} updated successfully", task.getId());
    }

    @Transactional
    public void assignTask(Long taskId, Long userId) {
        logger.info("Assigning task: {} to user: {}", taskId, userId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        User currentUser = authService.getCurrentUser();
        authorizationService.checkCanModifyTask(currentUser, task);

        User assignee = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        projectMemberRepository.findByProjectAndUser(task.getProject(), assignee)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this project"));

        User oldAssignee = task.getAssignedTo();
        task.setAssignedTo(assignee);
        taskRepository.save(task);

        String detail = oldAssignee == null
                ? "Assigned to " + assignee.getEmail()
                : "Reassigned from " + oldAssignee.getEmail() + " to " + assignee.getEmail();

        auditLogRepository.save(
                new AuditLog(
                        AuditAction.TASK_UPDATED,
                        task.getId(),
                        "TASK",
                        currentUser.getId(),
                        detail
                )
        );
        logger.info("Task {} assigned successfully", task.getId());
    }

    @Transactional
    public void deleteTask(Long taskId) {
        logger.info("Deleting task: {}", taskId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Task not found"));

        User currentUser = authService.getCurrentUser();

        authorizationService.checkCanDeleteTask(
                currentUser,
                task
        );

        auditLogRepository.save(
                new AuditLog(
                        AuditAction.TASK_DELETED,
                        task.getId(),
                        "TASK",
                        currentUser.getId(),
                        "Task deleted: " + task.getTitle()
                )
        );

        taskRepository.delete(task);
        logger.info("Task {} deleted successfully", task.getId());
    }
}