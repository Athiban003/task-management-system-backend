package com.athiban.task_management.service;

import com.athiban.task_management.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TokenCleanupService {
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenCleanupService(RefreshTokenRepository refreshTokenRepository){
        this.refreshTokenRepository=refreshTokenRepository;
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredTokens(){
        LocalDateTime cutoff=LocalDateTime.now().minusDays(30);

        int deleted = refreshTokenRepository.deleteByExpiresAtBeforeAndRevoked(cutoff, true);

        System.out.println("Cleaned up " + deleted + " expired/revoked tokens");
    }
}
