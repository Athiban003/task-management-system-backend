package com.athiban.task_management.service;

import com.athiban.task_management.dto.CreateProjectRequest;
import com.athiban.task_management.models.Project;
import com.athiban.task_management.models.User;
import com.athiban.task_management.repository.ProjectRespository;
import com.athiban.task_management.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {
    private ProjectRespository projectRespository;
    private UserRepository userRepository;

    public ProjectService(ProjectRespository projectRespository, UserRepository userRepository){
        this.projectRespository=projectRespository;
        this.userRepository = userRepository;
    }

    public void createProject(CreateProjectRequest request){
        User systemUser = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("System user not found"));
        Project project=new Project(systemUser);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setDeadline(request.getDeadline());
        project.activate();

        projectRespository.save(project);
    }
}
