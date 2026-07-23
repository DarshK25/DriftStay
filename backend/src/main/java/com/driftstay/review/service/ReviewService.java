package com.driftstay.review.service;

import com.driftstay.common.enums.ReviewStatus;
import com.driftstay.common.exception.ResourceNotFoundException;
import com.driftstay.review.entity.Review;
import com.driftstay.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Transactional
    public Review createReview(Review review) {
        review.setStatus(ReviewStatus.PENDING);
        Review saved = reviewRepository.save(review);
        log.info("Created review: {} for booking: {}", saved.getPublicId(), review.getBookingId());
        return saved;
    }

    @Transactional
    public Review approveReview(String publicId) {
        Review review = reviewRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + publicId));
        review.setStatus(ReviewStatus.APPROVED);
        Review saved = reviewRepository.save(review);
        log.info("Approved review: {}", publicId);
        return saved;
    }

    @Transactional
    public Review rejectReview(String publicId) {
        Review review = reviewRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + publicId));
        review.setStatus(ReviewStatus.REJECTED);
        Review saved = reviewRepository.save(review);
        log.info("Rejected review: {}", publicId);
        return saved;
    }

    public Review getReviewByPublicId(String publicId) {
        return reviewRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + publicId));
    }

    public Page<Review> getPropertyReviews(Long propertyId, Pageable pageable) {
        return reviewRepository.findByPropertyIdAndStatus(propertyId, ReviewStatus.APPROVED, pageable);
    }

    public Page<Review> getUserReviews(Long userId, Pageable pageable) {
        return reviewRepository.findByUserId(userId, pageable);
    }

    @Transactional
    public void deleteReview(String publicId) {
        Review review = reviewRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + publicId));
        reviewRepository.delete(review);
        log.info("Deleted review: {}", publicId);
    }
}
