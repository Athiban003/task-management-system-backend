package com.athiban.task_management.repository;

import com.athiban.task_management.models.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

}
