package com.athiban.task_management.service;

import com.athiban.task_management.AbstractIntegrationTest;
import com.athiban.task_management.dto.*;
import com.athiban.task_management.exception.UnauthorizedActionException;
import com.athiban.task_management.models.*;
import com.athiban.task_management.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class ProjectServiceIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuditLogRepository auditLogRepository;
    @Autowired
    private ProjectMemberRepository projectMemberRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskService taskService;

    private User testUser;

    @BeforeEach
    void setup(){
        taskRepository.deleteAll();
        projectMemberRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
        auditLogRepository.deleteAll();

        testUser=new User();
        testUser.setName("Test Manager");
        testUser.setEmail("manager@test.com");
        testUser.setPassword("password");
        testUser.setRole(Role.MANAGER);
        testUser=userRepository.save(testUser);

        authenticate(testUser);
    }

    // ======================
    // Helper Methods
    // ======================

    private void authenticate(User user) {

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + user.getRole().name()
                                )
                        )
                )
        );
    }

    private Project createProject(
            String name,
            String description,
            LocalDate deadline
    ) {

        CreateProjectRequest request = new CreateProjectRequest();

        request.setName(name);
        request.setDescription(description);
        request.setDeadline(deadline);

        projectService.createProject(request);

        return projectRepository.findAll().getFirst();
    }

    private User createUser(
            String name,
            String email,
            Role role
    ) {

        User user = new User();

        user.setName(name);
        user.setEmail(email);
        user.setPassword("password");
        user.setRole(role);

        return userRepository.save(user);
    }

    // ======================
    // Tests
    // ======================

    @Test
    void createProject_savesProjectToDataBase(){
        createProject(
                        "Integration Test Project",
                        "Testing with real database",
                        LocalDate.now().plusDays(30)
        );

        List<Project> projects=projectRepository.findAll();
        assertThat(projects).hasSize(1);
        Project savedProject = projects.getFirst();
        assertThat(savedProject.getName()).isEqualTo("Integration Test Project");
        assertThat(savedProject.getDescription()).isEqualTo("Testing with real database");
        assertThat(savedProject.getStatus()).isEqualTo(ProjectStatus.ACTIVE);
        assertThat(savedProject.getCreatedBy().getId()).isEqualTo(testUser.getId());

        // Assert creator is added as OWNER member
        List<ProjectMember> members = projectMemberRepository.findByProject(savedProject);
        assertThat(members).hasSize(1);

        ProjectMember ownerMember = members.getFirst();
        assertThat(ownerMember.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(ownerMember.getRole()).isEqualTo(ProjectMemberRole.OWNER);
    }

    @Test
    void updateProject_whenNotOwner_throwsUnauthorizedException() {
        Project project = createProject(
                "Manager Project",
                "Original Description",
                LocalDate.now().plusDays(30)
        );

        User anotherManager = createUser(
                "Another Manager",
                "another@test.com",
                Role.MANAGER
        );

        authenticate(anotherManager);

        UpdateProjectRequest updateRequest = new UpdateProjectRequest();
        updateRequest.setName("new Name");
        updateRequest.setDescription("Trying to modify");
        updateRequest.setDeadline(LocalDate.now().plusDays(60));

        assertThatThrownBy(() ->
                projectService.updateProject(project.getId(), updateRequest))
                .isInstanceOf(UnauthorizedActionException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    void updateProject_whenAdmin_canModifyAnyProject() {

        Project project = createProject(
                "Manager Project",
                "Original Description",
                LocalDate.now().plusDays(30)
        );

        User admin = createUser(
                "Admin User",
                "admin@test.com",
                Role.ADMIN
        );

        authenticate(admin);

        UpdateProjectRequest updateRequest =
                new UpdateProjectRequest();

        updateRequest.setName("Admin Updated");
        updateRequest.setDescription("Admin Override");
        updateRequest.setDeadline(
                LocalDate.now().plusDays(60)
        );

        projectService.updateProject(
                project.getId(),
                updateRequest
        );

        Project updatedProject =
                projectRepository.findById(project.getId())
                        .orElseThrow();

        assertThat(updatedProject.getName())
                .isEqualTo("Admin Updated");

        assertThat(updatedProject.getDescription())
                .isEqualTo("Admin Override");
    }

    @Test
    void updateProject_whenAdminOverride_auditLogShowsOverride() {
        Project project = createProject(
                "Manager Project",
                "Original Description",
                LocalDate.now().plusDays(30)
        );

        User admin = createUser(
                "Admin User",
                "admin@test.com",
                Role.ADMIN
        );

        authenticate(admin);

        UpdateProjectRequest updateRequest = new UpdateProjectRequest();
        updateRequest.setName("Admin Changed");
        updateRequest.setDescription("Override");
        updateRequest.setDeadline(LocalDate.now().plusDays(60));

        projectService.updateProject(project.getId(), updateRequest);

        List<AuditLog> auditLogs = auditLogRepository.findAll();

        // Should have 2 logs: PROJECT_CREATED and PROJECT_UPDATED
        assertThat(auditLogs).hasSize(2);

        // Find the update log
        AuditLog updateLog = auditLogs.stream()
                .filter(log -> log.getAction() == AuditAction.PROJECT_UPDATED)
                .findFirst()
                .orElseThrow();

        // Should contain [ADMIN OVERRIDE] prefix
        assertThat(updateLog.getDetails()).startsWith("[ADMIN OVERRIDE]");
    }

    @Test
    void whenUserAddedAsEditor_canModifyProject() {
        // Create project by testUser
        Project project = createProject(
                "Shared Project",
                "Testing shared access",
                LocalDate.now().plusDays(30)
        );

        // Create another manager
        User anotherManager = createUser(
                "Another Manager",
                "another@test.com",
                Role.MANAGER
        );

        // Add as EDITOR
        ProjectMember member = new ProjectMember(
                project,
                anotherManager,
                ProjectMemberRole.EDITOR
        );
        projectMemberRepository.save(member);

        authenticate(anotherManager);

        // Try to update (should work now)
        UpdateProjectRequest updateRequest = new UpdateProjectRequest();
        updateRequest.setName("Updated by Editor");
        updateRequest.setDescription("Should work");
        updateRequest.setDeadline(LocalDate.now().plusDays(60));

        projectService.updateProject(project.getId(), updateRequest);

        // Assert
        Project updated = projectRepository.findById(project.getId()).get();
        assertThat(updated.getName()).isEqualTo("Updated by Editor");
    }

    @Test
    void createTask_inOwnProject_succeeds() {
        Project project = createProject(
                "Project with Tasks",
                "Test",
                LocalDate.now().plusDays(30)
        );

        CreateTaskRequest taskRequest = new CreateTaskRequest();
        taskRequest.setTitle("Implement feature");
        taskRequest.setDescription("Build login");
        taskRequest.setAssignedToId(testUser.getId());

        taskService.createTask(project.getId(), taskRequest);

        List<Task> tasks = taskRepository.findByProject(project);
        assertThat(tasks).hasSize(1);
        assertThat(tasks.getFirst().getTitle()).isEqualTo("Implement feature");
        assertThat(tasks.getFirst().getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(tasks.getFirst().getAssignedTo().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void updateTask_byOwner_succeeds() {
        Project project = createProject(
                "Project",
                "Description",
                LocalDate.now().plusDays(30)
        );

        CreateTaskRequest createTaskRequest = new CreateTaskRequest();
        createTaskRequest.setTitle("Old Title");
        createTaskRequest.setDescription("Old Description");

        taskService.createTask(
                project.getId(),
                createTaskRequest
        );

        Task task = taskRepository.findAll().getFirst();

        UpdateTaskRequest updateRequest = new UpdateTaskRequest();
        updateRequest.setTitle("New Title");
        updateRequest.setDescription("New Description");

        taskService.updateTask(
                task.getId(),
                updateRequest
        );

        Task updatedTask = taskRepository
                .findById(task.getId())
                .orElseThrow();

        assertThat(updatedTask.getTitle())
                .isEqualTo("New Title");

        assertThat(updatedTask.getDescription())
                .isEqualTo("New Description");
    }

    @Test
    void editor_canCreateTask() {

        Project project = createProject(
                "Project",
                "Description",
                LocalDate.now().plusDays(30)
        );

        User editor = createUser(
                "Editor",
                "editor@test.com",
                Role.MANAGER
        );

        projectMemberRepository.save(
                new ProjectMember(
                        project,
                        editor,
                        ProjectMemberRole.EDITOR
                )
        );

        authenticate(editor);

        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Editor Task");
        request.setDescription("Created by editor");

        taskService.createTask(
                project.getId(),
                request
        );

        assertThat(
                taskRepository.findByProject(project)
        ).hasSize(1);
    }

    @Test
    void editor_canUpdateTask() {

        Project project = createProject(
                "Project",
                "Description",
                LocalDate.now().plusDays(30)
        );

        CreateTaskRequest createTaskRequest = new CreateTaskRequest();
        createTaskRequest.setTitle("Old Task");
        createTaskRequest.setDescription("Old Description");

        taskService.createTask(
                project.getId(),
                createTaskRequest
        );

        Task task = taskRepository.findAll().getFirst();

        User editor = createUser(
                "Editor User",
                "editor@test.com",
                Role.MANAGER
        );

        ProjectMember member = new ProjectMember(
                project,
                editor,
                ProjectMemberRole.EDITOR
        );

        projectMemberRepository.save(member);

        authenticate(editor);

        UpdateTaskRequest updateRequest = new UpdateTaskRequest();
        updateRequest.setTitle("Updated By Editor");
        updateRequest.setDescription("Editor Updated Description");

        taskService.updateTask(
                task.getId(),
                updateRequest
        );

        Task updatedTask = taskRepository
                .findById(task.getId())
                .orElseThrow();

        assertThat(updatedTask.getTitle())
                .isEqualTo("Updated By Editor");

        assertThat(updatedTask.getDescription())
                .isEqualTo("Editor Updated Description");
    }

    @Test
    void updateTaskStatus_byAssignee_succeeds() {
        Project project = createProject(
                "Project with Tasks",
                "Test",
                LocalDate.now().plusDays(30)
        );

        CreateTaskRequest taskRequest = new CreateTaskRequest();
        taskRequest.setTitle("Task");
        taskRequest.setDescription("Desc");
        taskRequest.setAssignedToId(testUser.getId());
        taskService.createTask(project.getId(), taskRequest);

        Task task = taskRepository.findAll().getFirst();

        // Update status
        UpdateTaskStatusRequest statusRequest = new UpdateTaskStatusRequest();
        statusRequest.setStatus(TaskStatus.IN_PROGRESS);

        taskService.updateTaskStatus(task.getId(), statusRequest);

        Task updated = taskRepository
                .findById(task.getId())
                .orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void deleteTask_byOwner_succeeds() {
        Project project = createProject(
                "Project",
                "Description",
                LocalDate.now().plusDays(30)
        );

        CreateTaskRequest taskRequest = new CreateTaskRequest();

        taskRequest.setTitle("Task To Delete");
        taskRequest.setDescription("Description");

        taskService.createTask(
                project.getId(),
                taskRequest
        );

        Task task = taskRepository.findAll().getFirst();

        taskService.deleteTask(task.getId());

        assertThat(taskRepository.findAll())
                .isEmpty();
    }

    @Test
    void updateTaskStatus_byUnauthorizedUser_throwsException() {
        Project project = createProject(
                "Project",
                "Description",
                LocalDate.now().plusDays(30)
        );

        CreateTaskRequest taskRequest = new CreateTaskRequest();

        taskRequest.setTitle("Task");
        taskRequest.setDescription("Description");

        taskService.createTask(
                project.getId(),
                taskRequest
        );

        Task task = taskRepository.findAll().getFirst();

        User anotherManager = userRepository.save(
                createUser(
                        "Another Manager",
                        "another@test.com",
                        Role.MANAGER
                )
        );

        authenticate(anotherManager);

        UpdateTaskStatusRequest statusRequest = new UpdateTaskStatusRequest();

        statusRequest.setStatus(TaskStatus.IN_PROGRESS);

        assertThatThrownBy(() ->
                taskService.updateTaskStatus(
                        task.getId(),
                        statusRequest
                ))
                .isInstanceOf(
                        UnauthorizedActionException.class
                );
    }

    @Test
    void assignTask_toNonMember_throwsException() {
        Project project = createProject(
                "Project",
                "Description",
                LocalDate.now().plusDays(30)
        );

        CreateTaskRequest taskRequest = new CreateTaskRequest();

        taskRequest.setTitle("Task");
        taskRequest.setDescription("Description");

        taskService.createTask(
                project.getId(),
                taskRequest
        );

        Task task = taskRepository.findAll().getFirst();

        User randomUser = createUser(
                        "Random User",
                        "random@test.com",
                        Role.MEMBER
        );

        assertThatThrownBy(() ->
                taskService.assignTask(
                        task.getId(),
                        randomUser.getId()
                ))
                .isInstanceOf(
                        IllegalArgumentException.class
                );
    }

    @Test
    void assignTask_toProjectMember_succeeds() {
        Project project = createProject(
                "Project",
                "Description",
                LocalDate.now().plusDays(30)
        );

        User member = createUser(
                "Member",
                "member@test.com",
                Role.MEMBER
        );

        projectMemberRepository.save(
                new ProjectMember(
                        project,
                        member,
                        ProjectMemberRole.VIEWER
                )
        );

        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Task");
        request.setDescription("Description");

        taskService.createTask(
                project.getId(),
                request
        );

        Task task = taskRepository.findAll().getFirst();

        taskService.assignTask(
                task.getId(),
                member.getId()
        );

        Task updated = taskRepository
                .findById(task.getId())
                .orElseThrow();

        assertThat(updated.getAssignedTo().getId())
                .isEqualTo(member.getId());
    }
}

