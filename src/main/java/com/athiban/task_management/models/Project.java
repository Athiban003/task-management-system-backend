
package com.athiban.task_management.models;

import com.athiban.task_management.exception.InvalidProjectStateException;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @JoinColumn(name = "created_by",nullable = false,updatable = false)
    private User createdBy;

    @Version
    @Column(nullable = false)
    private Long version;

    protected Project() {}

    public Project(User creator, String name, String description, LocalDate deadline) {
        this.createdBy = creator;
        this.name = name;
        this.description = description;
        this.deadline = deadline;
        this.status = ProjectStatus.ACTIVE;
    }

    @PrePersist
    protected void onCreate(){
        createdAt=LocalDateTime.now();
        updatedAt=LocalDateTime.now();
    }

    @PreUpdate
    protected  void onUpdate(){
        updatedAt=LocalDateTime.now();
    }

    public List<String> updateDetails(String name, String description, LocalDate deadline){

        if(this.status==ProjectStatus.ARCHIVED || this.status== ProjectStatus.COMPLETED){
            throw new InvalidProjectStateException("Cannot modify completed/archived project");
        }

        List<String> changes = new ArrayList<>();
        if (!this.name.equals(name)) {
            changes.add("name: " + this.name + " → " + name);
            this.name = name;
        }

        if (!this.description.equals(description)) {
            changes.add("description changed");
            this.description = description;
        }

        if (!this.deadline.equals(deadline)) {
            changes.add("deadline: " + this.deadline + " → " + deadline);
            this.deadline = deadline;
        }
        return changes;
    }

    public boolean changeStatus(ProjectStatus newStatus) {

        if(this.status==newStatus){
            return false;
        }

        if (this.status == ProjectStatus.ARCHIVED) {
            throw new InvalidProjectStateException("Archived project cannot change status");
        }

        if (this.status == ProjectStatus.COMPLETED && newStatus !=ProjectStatus.ARCHIVED) {
            throw new InvalidProjectStateException("Completed project can only be archived");
        }

        this.status = newStatus;
        return true;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public ProjectStatus getStatus() {
        return status;
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

    public Long getVersion() {
        return version;
    }
}
