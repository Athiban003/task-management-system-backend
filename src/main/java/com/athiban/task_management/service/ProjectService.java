package com.athiban.task_management.service;

import com.athiban.task_management.dto.CreateProjectRequest;
import com.athiban.task_management.repository.ProjectRespository;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {
    private ProjectRespository projectRespository;

    public ProjectService(ProjectRespository projectRespository){
        this.projectRespository=projectRespository;
    }

    public void createProject(CreateProjectRequest request){

    }
}
