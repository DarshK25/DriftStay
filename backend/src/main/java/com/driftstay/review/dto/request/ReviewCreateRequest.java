package com.driftstay.review.dto.request;

import com.driftstay.common.validation.CreateValidation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ReviewCreateRequest {
    @NotNull(groups = CreateValidation.class)
    private Long bookingId;
    @NotNull(groups = CreateValidation.class)
    @Min(value = 1, groups = CreateValidation.class) @Max(value = 5, groups = CreateValidation.class)
    private BigDecimal rating;
    private String title;
    private String reviewText;
}
