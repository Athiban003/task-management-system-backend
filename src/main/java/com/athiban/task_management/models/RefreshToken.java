package com.athiban.task_management.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean revoked=false;

    @Column(nullable = false,updatable = false)
    private LocalDateTime createdAt;

    protected RefreshToken(){}

    public RefreshToken(String token,User user ,LocalDateTime expiresAt){
        this.token=token;
        this.user=user;
        this.expiresAt=expiresAt;
    }

    @PrePersist
    protected void onCreate(){
        this.createdAt=LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }
    public String getToken() {
        return token;
    }
    public User getUser() {
        return user;
    }
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    public boolean isRevoked() {
        return revoked;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }
}
