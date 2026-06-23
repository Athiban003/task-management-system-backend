package com.athiban.task_management.controller;

import com.athiban.task_management.dto.CreateTaskRequest;
import com.athiban.task_management.dto.UpdateTaskRequest;
import com.athiban.task_management.dto.UpdateTaskStatusRequest;
import com.athiban.task_management.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<Void> createTask(@PathVariable Long projectId,
                                           @Valid @RequestBody CreateTaskRequest request) {
        taskService.createTask(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<Void> updateTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request) {

        taskService.updateTask(taskId, request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<Void> updateTaskStatus(@PathVariable Long projectId,
                                                 @PathVariable Long taskId,
                                                 @Valid @RequestBody UpdateTaskStatusRequest request) {
        taskService.updateTaskStatus(taskId, request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{taskId}/assign/{userId}")
    public ResponseEntity<Void> assignTask(@PathVariable Long projectId,
                                           @PathVariable Long taskId,
                                           @PathVariable Long userId) {
        taskService.assignTask(taskId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId) {

        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}