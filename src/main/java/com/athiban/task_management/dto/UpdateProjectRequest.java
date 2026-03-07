package com.athiban.task_management.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class UpdateProjectRequest {
    @NotBlank(message = "Project name is required.")
    @Size(min = 5, max = 100, message = "Project name must be between 5 and 100 characters.")
    private String name;

    @NotBlank(message = "Project description is required")
    @Size(max = 500, message = "project description cannot exceed 500 characters")
    private String description;


    @NotNull(message="project deadline is required")
    @FutureOrPresent(message="project deadline must be today or a future date")
    private LocalDate deadline;

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
}
