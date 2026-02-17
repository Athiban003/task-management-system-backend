package com.athiban.task_management.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class UpdateProjectRequest {
    @NotBlank
    @Size(min = 5, max = 100)
    private String name;

    @NotBlank
    @Size(max = 500)
    private String description;

    @NotNull
    @Future
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
