package com.athiban.task_management.security;

import com.athiban.task_management.exception.UnauthorizedActionException;
import com.athiban.task_management.models.Project;
import com.athiban.task_management.models.ProjectMemberRole;
import com.athiban.task_management.models.Role;
import com.athiban.task_management.models.User;
import com.athiban.task_management.repository.ProjectMemberRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {
    private final ProjectMemberRepository projectMemberRepository;

    public  AuthorizationService(ProjectMemberRepository projectMemberRepository){
        this.projectMemberRepository = projectMemberRepository;
    }
    public void checkCanCreateProject(User user){
        if(user.getRole()!=Role.ADMIN && user.getRole()!=Role.MANAGER){
            throw new UnauthorizedActionException("Only managers and administrators can create projects");
        }
    }

    public void checkCanModifyProject(User currentUser, Project project){
        boolean isAdmin=currentUser.getRole()== Role.ADMIN;
        boolean isOwner=project.getCreatedBy().getId().equals(currentUser.getId());
        boolean isEditor = projectMemberRepository.existsByProjectAndUserAndRole(
                project,
                currentUser,
                ProjectMemberRole.EDITOR
        );
        if (!(isAdmin || isOwner || isEditor)) {
            throw new UnauthorizedActionException(
                    "You are not authorized to modify this project"
            );
        }
    }

    public void checkCanDeleteProject(User currentUser, Project project) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isOwner = project.getCreatedBy().getId().equals(currentUser.getId());

        if (!(isAdmin || isOwner)) {
            throw new UnauthorizedActionException(
                    "You are not authorized to delete this project"
            );
        }
    }

    public void checkCanViewProject(User currentUser, Project project){
        boolean isAdmin=currentUser.getRole()==Role.ADMIN;
        boolean isOwner=project.getCreatedBy().getId().equals(currentUser.getId());
        boolean isMember = projectMemberRepository.existsByProjectAndUser(project, currentUser);

        if(!(isAdmin || isOwner || isMember)){
            throw new UnauthorizedActionException("You are not authorized to view this project");
        }
    }

    public void checkCanManageMembers(User currentUser, Project project) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isOwner = project.getCreatedBy().getId().equals(currentUser.getId());

        if (!(isAdmin || isOwner)) {
            throw new UnauthorizedActionException(
                    "Only project owner and administrators can manage members"
            );
        }
    }

    public boolean isAdminOverride(User currentUser,Project project){
        return currentUser.getRole()==Role.ADMIN
                && !project.getCreatedBy().getId().equals(currentUser.getId());
    }
}
