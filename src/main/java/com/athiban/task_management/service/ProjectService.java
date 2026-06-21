package com.athiban.task_management.service;

import com.athiban.task_management.dto.CreateProjectRequest;
import com.athiban.task_management.dto.UpdateProjectRequest;
import com.athiban.task_management.dto.UpdateProjectStatusRequest;
import com.athiban.task_management.exception.ProjectNotFoundException;
import com.athiban.task_management.exception.UnauthorizedActionException;
import com.athiban.task_management.models.*;
import com.athiban.task_management.repository.AuditLogRepository;
import com.athiban.task_management.repository.ProjectRepository;
import com.athiban.task_management.security.AuthorizationService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final AuthService authService;
    private final AuthorizationService authorizationService;
    private final AuditLogRepository auditLogRepository;

    public ProjectService(ProjectRepository projectRepository, AuthService authService,AuditLogRepository auditLogRepository,
                          AuthorizationService authorizationService){
        this.projectRepository = projectRepository;
        this.authService= authService;
        this.auditLogRepository=auditLogRepository;
        this.authorizationService=authorizationService;
    }

    @Transactional
    public void createProject(CreateProjectRequest request){
        User creator = authService.getCurrentUser();
        authorizationService.checkCanCreateProject(creator);

        Project project=new Project(creator, request.getName(),request.getDescription(),request.getDeadline());
        projectRepository.save(project);

        auditLogRepository.save(
                new AuditLog(
                        AuditAction.PROJECT_CREATED,
                        project.getId(),
                        "PROJECT",
                        creator.getId(),
                        "Project created"
                )
        );
    }

    @Transactional
    public void updateProject(Long projectId, UpdateProjectRequest request){
        Project project=projectRepository.findById(projectId)
                .orElseThrow(()->new ProjectNotFoundException(("Project not found")));

        User currentUser=authService.getCurrentUser();
        authorizationService.checkCanModifyProject(currentUser,project);

        List<String> changes = project.updateDetails(
                request.getName(),
                request.getDescription(),
                request.getDeadline()
        );

        if(!changes.isEmpty()){
            projectRepository.save(project);
            String details=String.join(", ",changes);

            if(authorizationService.isAdminOverride(currentUser,project)){
                details="[ADMIN OVERRIDE] "+details;
            }
            auditLogRepository.save(new AuditLog(
                AuditAction.PROJECT_UPDATED,
                project.getId(),
                "PROJECT",
                currentUser.getId(),
                    details
            ));
        }
    }

    @Transactional
    public  void updateProjectStatus(Long projectId, UpdateProjectStatusRequest request){
        Project project =projectRepository.findById(projectId)
                .orElseThrow(()->new ProjectNotFoundException(("Project not found")));

        User currentUser=authService.getCurrentUser();
        authorizationService.checkCanModifyProject(currentUser,project);

        if(!project.getCreatedBy().getId().equals(currentUser.getId())){
            throw new UnauthorizedActionException("You are not allowed to update the project");
        }

        ProjectStatus oldStatus=project.getStatus();
        boolean changed= project.changeStatus(request.getStatus());

        if(changed){
            projectRepository.save(project);
            String details = "status: " +oldStatus+"->"+ project.getStatus();

            if(authorizationService.isAdminOverride(currentUser,project)){
                details="[ADMIN OVERRIDE] "+details;
            }
            auditLogRepository.save(new AuditLog(
                AuditAction.PROJECT_STATUS_UPDATED,
                project.getId(),
                "PROJECT",
                currentUser.getId(),
                    details
            ));
        }
    }
}
