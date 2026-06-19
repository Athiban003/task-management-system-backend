package com.athiban.task_management.service;

import com.athiban.task_management.AbstractIntegrationTest;
import com.athiban.task_management.dto.CreateProjectRequest;
import com.athiban.task_management.dto.UpdateProjectRequest;
import com.athiban.task_management.exception.UnauthorizedActionException;
import com.athiban.task_management.models.*;
import com.athiban.task_management.repository.AuditLogRepository;
import com.athiban.task_management.repository.ProjectRepository;
import com.athiban.task_management.repository.UserRepository;
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

    private User testUser;

    @BeforeEach
    void setup(){
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
}

