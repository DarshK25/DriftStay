package com.driftstay.review.repository;

import com.driftstay.common.enums.ReviewStatus;
import com.driftstay.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByPublicId(String publicId);

    Optional<Review> findByBookingId(Long bookingId);

    @EntityGraph(attributePaths = {"images"})
    List<Review> findByPropertyIdOrderByCreatedAtDesc(Long propertyId);

    @EntityGraph(attributePaths = {"images"})
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Review> findByPropertyIdAndStatus(Long propertyId, ReviewStatus status, Pageable pageable);

    long countByPropertyIdAndStatus(Long propertyId, ReviewStatus status);
}
