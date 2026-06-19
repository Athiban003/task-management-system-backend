package com.athiban.task_management.models;

import com.athiban.task_management.exception.InvalidProjectStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProjectTest {
    private User testUser;
    private Project project;

    @BeforeEach
    void setUp(){
        testUser=new User();
        testUser.setName("test");
        testUser.setEmail("test@gmail.com");
        testUser.setPassword("secure123");
        testUser.setRole(Role.MANAGER);

        project = new Project(
                testUser,
                "Test Project",
                "Description",
                LocalDate.now().plusDays(30)
        );
    }

    @Test
    void whenStatusIsSame_thenChangeStatusReturnsFalse(){
        // Arrange: Project starts as ACTIVE (default)
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.ACTIVE);
        // Act: Try to change to ACTIVE again
        boolean changed=project.changeStatus(ProjectStatus.ACTIVE);
        // Assert: Should return false (no change happened)
        assertThat(changed).isFalse();
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.ACTIVE);
    }

    @Test
    void whenStatusIsDifferent_thenChangeStatusReturnsTrue(){
        // Arrange: Project starts as ACTIVE (default)
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.ACTIVE);
        // Act: Change to ON_HOLD
        boolean changed=project.changeStatus(ProjectStatus.ON_HOLD);
        // Assert: Should return true and status should change
        assertThat(changed).isTrue();
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.ON_HOLD);
    }

    @Test
    void whenProjectIsArchived_thenCannotChangeStatus(){
        // Arrange: Change project to ARCHIVED
        project.changeStatus(ProjectStatus.ARCHIVED);

        // Act & Assert: Trying to change archived project should throw exception
        assertThatThrownBy(()->project.changeStatus(ProjectStatus.ACTIVE))
                .isInstanceOf(InvalidProjectStateException.class)
                .hasMessage("Archived project cannot change status");
    }

    @Test
    void whenProjectIsCompleted_thenCanOnlyArchive() {
        // Arrange: Change project to COMPLETED
        project.changeStatus(ProjectStatus.COMPLETED);

        // Act & Assert: Can change to ARCHIVED
        boolean changed = project.changeStatus(ProjectStatus.ARCHIVED);
        assertThat(changed).isTrue();
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.ARCHIVED);
    }

    @Test
    void whenProjectIsCompleted_thenCannotChangeToActive() {
        // Arrange: Change project to COMPLETED
        project.changeStatus(ProjectStatus.COMPLETED);

        // Act & Assert: Cannot change to ACTIVE
        assertThatThrownBy(() -> project.changeStatus(ProjectStatus.ACTIVE))
                .isInstanceOf(InvalidProjectStateException.class)
                .hasMessage("Completed project can only be archived");
    }
}
