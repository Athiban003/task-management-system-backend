package com.athiban.task_management.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"}))
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectMemberRole role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime addedAt;

    protected ProjectMember() {}

    public ProjectMember(Project project, User user, ProjectMemberRole role) {
        this.project = project;
        this.user = user;
        this.role = role;
    }

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public Project getProject() { return project; }
    public User getUser() { return user; }
    public ProjectMemberRole getRole() { return role; }
    public LocalDateTime getAddedAt() { return addedAt; }

    // Setters
    public void setRole(ProjectMemberRole role) { this.role = role; }
}