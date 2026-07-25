package com.driftstay.payment.service;

import com.driftstay.payment.entity.IdempotencyKey;
import com.driftstay.payment.repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Manages idempotency keys to prevent duplicate payment processing.
 * 
 * Flow:
 * 1. Client sends Idempotency-Key header with payment request
 * 2. Server checks if key exists:
 *    a. If exists and COMPLETED → return cached response (no charge)
 *    b. If exists and IN_PROGRESS → return 409 Conflict (still processing)
 *    c. If not exists → create IN_PROGRESS, process payment, mark COMPLETED
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyKeyRepository idempotencyKeyRepository;

    /**
     * Try to acquire the idempotency key for processing.
     * @return Optional with existing key if already in progress/completed
     */
    @Transactional
    public Optional<IdempotencyKey> tryAcquire(String key, String entityType, Long entityId) {
        Optional<IdempotencyKey> existing = idempotencyKeyRepository.findByIdempotencyKey(key);

        if (existing.isPresent()) {
            IdempotencyKey existingKey = existing.get();

            if (existingKey.getStatus() == IdempotencyKey.IdempotencyStatus.COMPLETED) {
                log.info("Idempotency key {} already completed for {}:{}", key, entityType, entityId);
                return existing;
            }

            if (existingKey.getStatus() == IdempotencyKey.IdempotencyStatus.IN_PROGRESS) {
                log.warn("Idempotency key {} still in progress for {}:{}", key, entityType, entityId);
                return existing;
            }
        }

        // Create new idempotency key
        IdempotencyKey idempotencyKey = new IdempotencyKey();
        idempotencyKey.setIdempotencyKey(key);
        idempotencyKey.setEntityType(entityType);
        idempotencyKey.setEntityId(entityId);
        idempotencyKey.setStatus(IdempotencyKey.IdempotencyStatus.IN_PROGRESS);
        idempotencyKey.setCreatedAt(LocalDateTime.now());
        idempotencyKey.setExpiresAt(LocalDateTime.now().plusHours(24));

        idempotencyKeyRepository.save(idempotencyKey);

        return Optional.empty();
    }

    /**
     * Mark an idempotency key as completed with response details.
     */
    @Transactional
    public void complete(String key, String responseBody, int responseStatus) {
        idempotencyKeyRepository.findByIdempotencyKey(key).ifPresent(existing -> {
            existing.setStatus(IdempotencyKey.IdempotencyStatus.COMPLETED);
            existing.setResponseBody(responseBody);
            existing.setResponseStatus(responseStatus);
            idempotencyKeyRepository.save(existing);
        });
    }

    /**
     * Clean up expired keys.
     */
    @Transactional
    public void cleanupExpiredKeys() {
        idempotencyKeyRepository.deleteExpiredKeys(LocalDateTime.now());
    }
}
