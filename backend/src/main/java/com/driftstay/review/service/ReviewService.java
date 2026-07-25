package com.driftstay.review.service;

import com.driftstay.common.enums.ReviewStatus;
import com.driftstay.gallery.service.ImageStorageService;
import com.driftstay.review.entity.Review;
import com.driftstay.review.entity.ReviewImage;
import com.driftstay.review.repository.ReviewRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ImageStorageService imageStorageService;
    private final EntityManager entityManager;

    @Transactional
    public Review createReview(Long bookingId, Long propertyId, Long userId,
                               BigDecimal rating, String title, String text,
                               List<MultipartFile> images) {
        // Check if review already exists for this booking
        if (reviewRepository.findByBookingId(bookingId).isPresent()) {
            throw new IllegalArgumentException("A review already exists for this booking");
        }

        Review review = new Review();
        review.setPublicId(UUID.randomUUID().toString().replace("-", "").substring(0, 26));
        review.setBookingId(bookingId);
        review.setPropertyId(propertyId);
        review.setUserId(userId);
        review.setRating(rating);
        review.setTitle(title);
        review.setReviewText(text);
        review.setStatus(ReviewStatus.PENDING);

        review = reviewRepository.save(review);

        // Upload images if provided
        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                String url = imageStorageService.uploadImage(file, "reviews/" + review.getId());
                ReviewImage image = new ReviewImage();
                image.setReview(review);
                image.setImageUrl(url);
                image.setDisplayOrder(review.getImages().size() + 1);
                entityManager.persist(image);
            }
        }

        log.info("Review created: id={}, booking={}, rating={}", review.getId(), bookingId, rating);
        return review;
    }

    @Transactional
    public Review updateReview(Long reviewId, BigDecimal rating, String title, String text) {
        Review review = getReview(reviewId);
        if (rating != null) review.setRating(rating);
        if (title != null) review.setTitle(title);
        if (text != null) review.setReviewText(text);
        return reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    @Transactional
    public Review approveReview(Long reviewId) {
        Review review = getReview(reviewId);
        review.setStatus(ReviewStatus.APPROVED);
        return reviewRepository.save(review);
    }

    public Review getReview(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + id));
    }

    public List<Review> getPropertyReviews(Long propertyId) {
        return reviewRepository.findByPropertyIdOrderByCreatedAtDesc(propertyId);
    }

    public List<Review> getUserReviews(Long userId) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Page<Review> getApprovedPropertyReviews(Long propertyId, Pageable pageable) {
        return reviewRepository.findByPropertyIdAndStatus(propertyId, ReviewStatus.APPROVED, pageable);
    }
}
