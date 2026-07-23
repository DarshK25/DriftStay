package com.driftstay.review.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ReviewResponse {
    private Long id;
    private String publicId;
    private Long bookingId;
    private Long propertyId;
    private Long userId;
    private String userName;
    private BigDecimal rating;
    private String title;
    private String reviewText;
    private String status;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
}
