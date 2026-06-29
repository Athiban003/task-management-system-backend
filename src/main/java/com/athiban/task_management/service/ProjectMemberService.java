package com.athiban.task_management.service;

import com.athiban.task_management.dto.ProjectMemberResponse;
import com.athiban.task_management.mapper.ProjectMemberMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.athiban.task_management.dto.AddMemberRequest;
import com.athiban.task_management.exception.ProjectNotFoundException;
import com.athiban.task_management.models.*;
import com.athiban.task_management.repository.ProjectMemberRepository;
import com.athiban.task_management.repository.ProjectRepository;
import com.athiban.task_management.repository.UserRepository;
import com.athiban.task_management.security.AuthorizationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectMemberService {
    private static final Logger logger = LoggerFactory.getLogger(ProjectMemberService.class);

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

    public Page<ProjectMemberResponse> getMembers(Long projectId, int page, int size) {
        User currentUser = authService.getCurrentUser();

        Project project = projectRepository.findById(projectId)
                        .orElseThrow(() -> new ProjectNotFoundException("Project not found"));

        authorizationService.checkCanViewProject(currentUser, project);

        Pageable pageable = PageRequest.of(page, size, Sort.by("addedAt").descending());

        Page<ProjectMember> members =projectMemberRepository.findByProject(project, pageable);

        return members.map(ProjectMemberMapper::toResponse);
    }

    @Transactional
    public void addMember(Long projectId, AddMemberRequest request) {
        logger.info(
                "Adding user {} to project {}",
                request.getUserId(),
                projectId
        );
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
        logger.info(
                "User {} added to project {} as {}",
                userToAdd.getId(),
                project.getId(),
                request.getRole()
        );
    }

    @Transactional
    public void removeMember(Long projectId, Long userId) {
        logger.info(
                "Removing user {} from project {}",
                userId,
                projectId
        );
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));

        User currentUser = authService.getCurrentUser();
        authorizationService.checkCanManageMembers(currentUser, project);

        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ProjectMember member = projectMemberRepository.findByProjectAndUser(project, userToRemove)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this project"));

        projectMemberRepository.delete(member);
        logger.info(
                "User {} removed from project {}",
                userId,
                projectId
        );
    }

    @Transactional
    public void updateMemberRole(Long projectId, Long userId, ProjectMemberRole newRole) {
        logger.info(
                "Updating role for user {} in project {}",
                userId,
                projectId
        );
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));

        User currentUser = authService.getCurrentUser();
        authorizationService.checkCanManageMembers(currentUser, project);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ProjectMember member = projectMemberRepository.findByProjectAndUser(project, user)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this project"));

        ProjectMemberRole oldRole = member.getRole();
        member.setRole(newRole);
        projectMemberRepository.save(member);
        logger.info(
                "Role changed for user {} from {} to {}",
                userId,
                oldRole,
                newRole
        );
    }
}