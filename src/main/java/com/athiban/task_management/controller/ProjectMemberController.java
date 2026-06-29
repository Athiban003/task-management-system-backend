package com.athiban.task_management.controller;

import com.athiban.task_management.dto.AddMemberRequest;
import com.athiban.task_management.dto.ProjectMemberResponse;
import com.athiban.task_management.dto.UpdateMemberRoleRequest;
import com.athiban.task_management.service.ProjectMemberService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    public ProjectMemberController(ProjectMemberService projectMemberService) {
        this.projectMemberService = projectMemberService;
    }

    @GetMapping
    public ResponseEntity<Page<ProjectMemberResponse>> getMembers(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                projectMemberService.getMembers(
                        projectId,
                        page,
                        size
                )
        );
    }

    @PostMapping
    public ResponseEntity<Void> addMember(@PathVariable Long projectId,
                                          @Valid @RequestBody AddMemberRequest request) {
        projectMemberService.addMember(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long projectId,
                                             @PathVariable Long userId) {
        projectMemberService.removeMember(projectId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<Void> updateMemberRole(@PathVariable Long projectId,
                                                 @PathVariable Long userId,
                                                 @Valid @RequestBody UpdateMemberRoleRequest request) {
        projectMemberService.updateMemberRole(projectId, userId, request.getRole());
        return ResponseEntity.noContent().build();
    }
}