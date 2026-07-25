package com.driftstay.payment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull
    private Long bookingId;

    @NotNull
    @Min(1)
    private BigDecimal amount;

    @NotBlank
    private String currency;

    @NotBlank
    private String paymentMethod;

    @NotBlank
    private String provider;

    private String description;

    private Map<String, String> metadata;
}
