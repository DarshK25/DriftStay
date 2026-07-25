package com.driftstay.payment.repository;

import com.driftstay.payment.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {

    Optional<IdempotencyKey> findByIdempotencyKey(String idempotencyKey);

    @Modifying
    @Query("DELETE FROM IdempotencyKey k WHERE k.expiresAt < :now")
    void deleteExpiredKeys(@Param("now") LocalDateTime now);
}
