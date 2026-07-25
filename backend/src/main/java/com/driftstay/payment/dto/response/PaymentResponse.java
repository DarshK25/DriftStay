package com.driftstay.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private boolean success;
    private String providerPaymentId;
    private String providerOrderId;
    private String transactionReference;
    private String status;
    private String message;
    private BigDecimal amount;
    private String currency;
    private String gatewayResponse;
}
