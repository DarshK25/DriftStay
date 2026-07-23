package com.driftstay.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ReviewCreateRequest {
    @NotNull
    private Long bookingId;
    @NotNull
    @Min(1) @Max(5)
    private BigDecimal rating;
    private String title;
    private String reviewText;
}
