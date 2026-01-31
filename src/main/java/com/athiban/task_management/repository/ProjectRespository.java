package com.athiban.task_management.repository;

import com.athiban.task_management.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRespository extends JpaRepository<Project , Long> {
}
