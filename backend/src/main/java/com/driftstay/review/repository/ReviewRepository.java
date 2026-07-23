package com.driftstay.review.repository;

import com.driftstay.common.enums.ReviewStatus;
import com.driftstay.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByPublicId(String publicId);
    Optional<Review> findByBookingId(Long bookingId);
    Page<Review> findByPropertyId(Long propertyId, Pageable pageable);
    Page<Review> findByPropertyIdAndStatus(Long propertyId, ReviewStatus status, Pageable pageable);
    Page<Review> findByUserId(Long userId, Pageable pageable);
}
