package com.driftstay.review.controller;

import com.driftstay.review.entity.Review;
import com.driftstay.review.service.ReviewService;
import com.driftstay.security.filter.JwtUser;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<Review> createReview(
            @RequestParam Long bookingId,
            @RequestParam Long propertyId,
            @RequestParam @Min(1) @Max(5) BigDecimal rating,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<MultipartFile> images,
            @AuthenticationPrincipal JwtUser currentUser) {
        Review review = reviewService.createReview(
                bookingId, propertyId, currentUser.getUserId(),
                rating, title, text, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(
            @PathVariable Long id,
            @RequestParam(required = false) @Min(1) @Max(5) BigDecimal rating,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String text,
            @AuthenticationPrincipal JwtUser currentUser) {
        return ResponseEntity.ok(reviewService.updateReview(id, rating, title, text));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUser currentUser) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<Review>> getPropertyReviews(@PathVariable Long propertyId) {
        return ResponseEntity.ok(reviewService.getPropertyReviews(propertyId));
    }

    @GetMapping("/user/me")
    public ResponseEntity<List<Review>> getMyReviews(@AuthenticationPrincipal JwtUser currentUser) {
        return ResponseEntity.ok(reviewService.getUserReviews(currentUser.getUserId()));
    }
}
