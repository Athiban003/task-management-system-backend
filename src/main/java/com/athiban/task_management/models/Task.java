package com.athiban.task_management.models;

import com.athiban.task_management.exception.InvalidProjectStateException;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to", nullable = true)
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Task() {}

    public Task(Project project, User createdBy, String title, String description) {
        this.project = project;
        this.createdBy = createdBy;
        this.title = title;
        this.description = description;
        this.status = TaskStatus.TODO;
        this.assignedTo = null;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public List<String> updateDetails(String title, String description) {

        List<String> changes = new ArrayList<>();

        if (!this.title.equals(title)) {
            changes.add("title: " + this.title + " -> " + title);
            this.title = title;
        }

        if (!this.description.equals(description)) {
            changes.add("description changed");
            this.description = description;
        }

        return changes;
    }

    // State machine: validate status transitions
    public boolean changeStatus(TaskStatus newStatus) {
        if (this.status == newStatus) {
            return false;
        }

        this.status = newStatus;
        return true;
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TaskStatus getStatus() { return status; }
    public Project getProject() { return project; }
    public User getAssignedTo() { return assignedTo; }
    public User getCreatedBy() { return createdBy; }
    public Long getVersion() { return version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }
}