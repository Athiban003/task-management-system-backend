package com.athiban.task_management.controller;

import com.athiban.task_management.dto.CreateProjectRequest;
import com.athiban.task_management.dto.UpdateProjectRequest;
import com.athiban.task_management.dto.UpdateProjectStatusRequest;
import com.athiban.task_management.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<Void> createProject(@Valid @RequestBody CreateProjectRequest request){
        projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public  ResponseEntity<Void> updateProject(@PathVariable Long id, @Valid @RequestBody UpdateProjectRequest request){
        projectService.updateProject(id,request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateProjectStatus(@PathVariable Long id, @Valid @RequestBody UpdateProjectStatusRequest request){
        projectService.updateProjectStatus(id,request);
        return ResponseEntity.noContent().build();
    }
}

