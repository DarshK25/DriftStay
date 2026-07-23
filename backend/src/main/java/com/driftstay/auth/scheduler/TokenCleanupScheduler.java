package com.driftstay.auth.scheduler;

import com.driftstay.auth.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.driftstay.auth.repository.RefreshTokenRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistService tokenBlacklistService;

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void purgeExpiredTokens() {
        int deleted = refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Purged {} expired refresh tokens", deleted);
        }
    }

    @Scheduled(cron = "0 0 4 * * ?")
    @Transactional
    public void purgeRevokedTokens() {
        int deleted = refreshTokenRepository.deleteRevokedOlderThan(LocalDateTime.now().minusDays(7));
        if (deleted > 0) {
            log.info("Purged {} revoked refresh tokens older than 7 days", deleted);
        }
    }

    @Scheduled(cron = "0 0 */6 * * ?")
    public void cleanupBlacklist() {
        tokenBlacklistService.removeExpired();
        log.debug("Cleaned up expired blacklist entries");
    }
}
