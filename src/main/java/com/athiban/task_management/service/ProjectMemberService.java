package com.athiban.task_management.service;

import com.athiban.task_management.dto.AddMemberRequest;
import com.athiban.task_management.exception.ProjectNotFoundException;
import com.athiban.task_management.models.*;
import com.athiban.task_management.repository.ProjectMemberRepository;
import com.athiban.task_management.repository.ProjectRepository;
import com.athiban.task_management.repository.UserRepository;
import com.athiban.task_management.security.AuthorizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final AuthorizationService authorizationService;

    public ProjectMemberService(ProjectMemberRepository projectMemberRepository,
                                ProjectRepository projectRepository,
                                UserRepository userRepository,
                                AuthService authService,
                                AuthorizationService authorizationService) {
        this.projectMemberRepository = projectMemberRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.authService = authService;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public void addMember(Long projectId, AddMemberRequest request) {
        // Load project
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));

        // Check authorization (only owner or admin can add members)
        User currentUser = authService.getCurrentUser();
        authorizationService.checkCanManageMembers(currentUser, project);

        // Load user to add
        User userToAdd = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if already a member
        if (projectMemberRepository.findByProjectAndUser(project, userToAdd).isPresent()) {
            throw new IllegalArgumentException("User is already a member of this project");
        }

        // Add member
        ProjectMember member = new ProjectMember(
                project,
                userToAdd,
                request.getRole()
        );
        projectMemberRepository.save(member);
    }

    @Transactional
    public void removeMember(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));

        User currentUser = authService.getCurrentUser();
        authorizationService.checkCanManageMembers(currentUser, project);

        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ProjectMember member = projectMemberRepository.findByProjectAndUser(project, userToRemove)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this project"));

        projectMemberRepository.delete(member);
    }

    @Transactional
    public void updateMemberRole(Long projectId, Long userId, ProjectMemberRole newRole) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));

        User currentUser = authService.getCurrentUser();
        authorizationService.checkCanManageMembers(currentUser, project);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ProjectMember member = projectMemberRepository.findByProjectAndUser(project, user)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this project"));

        member.setRole(newRole);
        projectMemberRepository.save(member);
    }
}