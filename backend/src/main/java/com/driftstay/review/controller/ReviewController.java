package com.driftstay.review.controller;

import com.driftstay.auth.service.AuthService;
import com.driftstay.common.dto.ApiResponse;
import com.driftstay.common.dto.PagedResponse;
import com.driftstay.common.validation.CreateValidation;
import com.driftstay.review.dto.request.ReviewCreateRequest;
import com.driftstay.review.dto.response.ReviewResponse;
import com.driftstay.review.mapper.ReviewMapper;
import com.driftstay.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Review management endpoints")
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewMapper reviewMapper;
    private final AuthService authService;

    @PostMapping
    @Operation(summary = "Create a review for a booking")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Validated(CreateValidation.class) @RequestBody ReviewCreateRequest request) {
        var user = authService.getCurrentUser(userDetails);
        var review = reviewMapper.toEntity(request);
        review.setUserId(user.getId());
        var saved = reviewService.createReview(review);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(reviewMapper.toResponse(saved)));
    }

    @GetMapping("/{publicId}")
    @Operation(summary = "Get review details")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReview(@PathVariable String publicId) {
        var review = reviewService.getReviewByPublicId(publicId);
        return ResponseEntity.ok(ApiResponse.success("Review found", reviewMapper.toResponse(review)));
    }

    @GetMapping("/property/{propertyId}")
    @Operation(summary = "Get approved reviews for a property")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getPropertyReviews(
            @PathVariable Long propertyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = reviewService.getPropertyReviews(propertyId,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(reviewMapper::toResponse);
        return ResponseEntity.ok(ApiResponse.paginated("Reviews found", PagedResponse.of(result)));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's reviews")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getMyReviews(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var user = authService.getCurrentUser(userDetails);
        var result = reviewService.getUserReviews(user.getId(),
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(reviewMapper::toResponse);
        return ResponseEntity.ok(ApiResponse.paginated("Reviews found", PagedResponse.of(result)));
    }

    @PostMapping("/{publicId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve a review (admin)")
    public ResponseEntity<ApiResponse<ReviewResponse>> approveReview(@PathVariable String publicId) {
        var review = reviewService.approveReview(publicId);
        return ResponseEntity.ok(ApiResponse.success("Review approved", reviewMapper.toResponse(review)));
    }

    @PostMapping("/{publicId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject a review (admin)")
    public ResponseEntity<ApiResponse<ReviewResponse>> rejectReview(@PathVariable String publicId) {
        var review = reviewService.rejectReview(publicId);
        return ResponseEntity.ok(ApiResponse.success("Review rejected", reviewMapper.toResponse(review)));
    }

    @DeleteMapping("/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a review")
    public ResponseEntity<Void> deleteReview(@PathVariable String publicId) {
        reviewService.deleteReview(publicId);
        return ResponseEntity.noContent().build();
    }
}
