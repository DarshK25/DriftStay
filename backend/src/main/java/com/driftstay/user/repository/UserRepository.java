package com.driftstay.user.repository;

import com.driftstay.user.entity.User;
import com.driftstay.common.enums.UserStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Auth & Lookup
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByPublicId(String publicId);
    
    // Strict Status filtering for active business flows
    Optional<User> findByEmailIgnoreCaseAndStatus(String email, UserStatus status);
    Optional<User> findByPublicIdAndStatus(String publicId, UserStatus status); 
    Optional<User> findByPhoneAndStatus(String phone, UserStatus status);

    // Validation checks
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByPhone(String phone);
    
    // High-performance paginated lookups (No heavy count queries)
    Slice<User> findByStatus(UserStatus status, Pageable pageable);
}