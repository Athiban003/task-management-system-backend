package com.athiban.task_management.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction action;

    @Column(nullable = false)
    private Long entityId;

    @Column(nullable = false, length = 50)
    private String entityType;

    @Column(nullable = false)
    private Long performedBy;

    @Column(length = 1000)
    private String details;

    @Column(nullable = false, updatable = false)
    private LocalDateTime performedAt;

    protected AuditLog(){}

    public AuditLog(AuditAction action , Long entityId, String entityType, Long performedBy, String details){
        this.action=action;
        this.entityId=entityId;
        this.entityType=entityType;
        this.performedBy=performedBy;
        this.details=details;
        this.performedAt=LocalDateTime.now();
    }

    public AuditAction getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }

}
