package com.athiban.task_management.security;

import com.athiban.task_management.exception.UnauthorizedActionException;
import com.athiban.task_management.models.*;
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
        boolean isOwner = projectMemberRepository.existsByProjectAndUserAndRole(
                project,
                currentUser,
                ProjectMemberRole.OWNER
        );
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

    public void checkCanUpdateStatus(User currentUser, Project project){
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        boolean isOwner = projectMemberRepository.existsByProjectAndUserAndRole(
                project,
                currentUser,
                ProjectMemberRole.OWNER
        );

        boolean isEditor =
                projectMemberRepository.existsByProjectAndUserAndRole(
                        project,
                        currentUser,
                        ProjectMemberRole.EDITOR
                );

        if(!(isAdmin || isOwner || isEditor)){
            throw new UnauthorizedActionException(
                    "You are not authorized to update project status"
            );
        }
    }

    public void checkCanDeleteProject(User currentUser, Project project) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isOwner = projectMemberRepository.existsByProjectAndUserAndRole(
                project,
                currentUser,
                ProjectMemberRole.OWNER
        );
        if (!(isAdmin || isOwner)) {
            throw new UnauthorizedActionException(
                    "You are not authorized to delete this project"
            );
        }
    }

    public void checkCanViewProject(User currentUser, Project project){
        boolean isAdmin=currentUser.getRole()==Role.ADMIN;
        boolean isOwner = projectMemberRepository.existsByProjectAndUserAndRole(
                project,
                currentUser,
                ProjectMemberRole.OWNER
        );
        boolean isMember = projectMemberRepository.existsByProjectAndUser(project, currentUser);

        if(!(isAdmin || isOwner || isMember)){
            throw new UnauthorizedActionException("You are not authorized to view this project");
        }
    }

    public void checkCanManageMembers(User currentUser, Project project) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isOwner = projectMemberRepository.existsByProjectAndUserAndRole(
                project,
                currentUser,
                ProjectMemberRole.OWNER
        );
        if (!(isAdmin || isOwner)) {
            throw new UnauthorizedActionException(
                    "Only project owner and administrators can manage members"
            );
        }
    }

    public void checkCanCreateTask(User currentUser, Project project) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        boolean isOwner = projectMemberRepository.existsByProjectAndUserAndRole(
                        project,
                        currentUser,
                        ProjectMemberRole.OWNER
                );

        boolean isEditor = projectMemberRepository.existsByProjectAndUserAndRole(
                        project,
                        currentUser,
                        ProjectMemberRole.EDITOR
                );

        if (!(isAdmin || isOwner || isEditor)) {
            throw new UnauthorizedActionException(
                    "You are not authorized to create tasks in this project"
            );
        }
    }

    public void checkCanModifyTask(User currentUser, Task task) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        boolean isOwner = projectMemberRepository.existsByProjectAndUserAndRole(
                task.getProject(),
                currentUser,
                ProjectMemberRole.OWNER
        );

        boolean isEditor = projectMemberRepository.existsByProjectAndUserAndRole(
                                task.getProject(),
                                currentUser,
                                ProjectMemberRole.EDITOR
                        );

        if (!(isAdmin || isOwner || isEditor)) {
            throw new UnauthorizedActionException(
                    "You are not authorized to modify this task"
            );
        }
    }

    public void checkCanUpdateTaskStatus(User currentUser, Task task) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        boolean isOwner = projectMemberRepository.existsByProjectAndUserAndRole(
                task.getProject(),
                currentUser,
                ProjectMemberRole.OWNER
        );

        boolean isAssignee = task.getAssignedTo() != null
                && task.getAssignedTo().getId().equals(currentUser.getId());

        if (!(isAdmin || isOwner || isAssignee)) {
            throw new UnauthorizedActionException(
                    "You are not authorized to update task status"
            );
        }
    }

    public void checkCanDeleteTask(User currentUser, Task task) {

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        boolean isOwner = projectMemberRepository.existsByProjectAndUserAndRole(
                task.getProject(),
                currentUser,
                ProjectMemberRole.OWNER
        );

        if (!(isAdmin || isOwner)) {
            throw new UnauthorizedActionException(
                    "You are not authorized to delete this task"
            );
        }
    }

    public boolean isAdminOverride(User currentUser,Project project){
        return currentUser.getRole()==Role.ADMIN
                && !projectMemberRepository.existsByProjectAndUserAndRole(
                project,
                currentUser,
                ProjectMemberRole.OWNER
        );
    }
}
