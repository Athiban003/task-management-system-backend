package com.athiban.task_management.repository;

import com.athiban.task_management.models.RefreshToken;
import com.athiban.task_management.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :cutoff OR rt.revoked = true")
    int deleteByExpiresAtBeforeAndRevoked(@Param("cutoff") LocalDateTime cutoff, boolean revoked);
}
