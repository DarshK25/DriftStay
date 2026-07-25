package com.driftstay.payment.controller;

import com.driftstay.payment.dto.request.PaymentRequest;
import com.driftstay.payment.dto.response.PaymentResponse;
import com.driftstay.payment.service.PaymentService;
import com.driftstay.payment.service.RefundService;
import com.driftstay.security.filter.JwtUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final RefundService refundService;

    /**
     * Process a payment for a booking.
     * POST /api/payments
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @AuthenticationPrincipal JwtUser currentUser) {
        log.debug("Payment request for booking {}: amount={} {} (idempotencyKey={})", 
                request.getBookingId(), request.getAmount(), request.getCurrency(), idempotencyKey);
        
        PaymentResponse response = paymentService.processPayment(
                request.getBookingId(),
                request.getAmount(),
                request.getCurrency(),
                request.getPaymentMethod(),
                request.getProvider(),
                idempotencyKey
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Process a refund for a payment.
     * POST /api/payments/{id}/refund
     */
    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable Long id,
            @RequestParam @NotNull @Min(1) BigDecimal amount,
            @RequestParam @NotBlank String reason,
            @AuthenticationPrincipal JwtUser currentUser) {
        log.debug("Refund request for payment {}: amount={}", id, amount);
        PaymentResponse response = refundService.processRefund(id, amount, reason);
        return ResponseEntity.ok(response);
    }

    /**
     * Get payment details.
     * GET /api/payments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
        // TODO: Implement payment lookup
        return ResponseEntity.notFound().build();
    }
}
