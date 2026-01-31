
package com.athiban.task_management.models;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 100)
    private String name;

    @Column(nullable = false,length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable =false)
    private ProjectStatus status;

    @Column(nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "created_by",nullable = false)
    private User createdBy;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDeadline() {
        return deadline;
    }
    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void activate() {
        if (this.status == ProjectStatus.ARCHIVED) {
            throw new IllegalStateException("Archived project cannot be activated");
        }
        this.status = ProjectStatus.ACTIVE;
    }

    public void putOnHold() {
        if (this.status == ProjectStatus.COMPLETED) {
            throw new IllegalStateException("Completed project cannot be put on hold");
        }
        if (this.status == ProjectStatus.ARCHIVED) {
            throw new IllegalStateException("Archived project cannot be put on hold");
        }
        this.status = ProjectStatus.ON_HOLD;
    }

    public void complete() {
        if (this.status == ProjectStatus.ARCHIVED) {
            throw new IllegalStateException("Archived project cannot be completed");
        }
        this.status = ProjectStatus.COMPLETED;
    }

    public void archive() {
        this.status = ProjectStatus.ARCHIVED;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public User getCreatedBy() {
        return createdBy;
    }

}
