package com.driftstay.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Idempotency key store for preventing duplicate payment processing.
 * When a client sends an Idempotency-Key header, the key + response are stored.
 * If the same key arrives again, the previous response is returned.
 * This prevents double charges even if the user presses "Pay" 5 times.
 */
@Entity
@Table(name = "idempotency_key",
        indexes = {
                @Index(name = "idx_idempotency_key_key", columnList = "idempotency_key", unique = true),
                @Index(name = "idx_idempotency_key_expiry", columnList = "expires_at")
        }
)
@Getter
@Setter
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, length = 255, unique = true)
    private String idempotencyKey;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private IdempotencyStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (expiresAt == null) {
            expiresAt = createdAt.plusHours(24); // Keys expire after 24 hours
        }
    }

    public enum IdempotencyStatus {
        IN_PROGRESS, COMPLETED, EXPIRED
    }
}
