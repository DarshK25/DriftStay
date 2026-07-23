package com.driftstay.payment.controller;

import com.driftstay.common.dto.ApiResponse;
import com.driftstay.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing endpoints")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{providerPaymentId}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Confirm a payment")
    public ResponseEntity<ApiResponse<String>> confirmPayment(@PathVariable String providerPaymentId) {
        paymentService.confirmPayment(providerPaymentId);
        return ResponseEntity.ok(ApiResponse.success("Payment confirmed", providerPaymentId));
    }

    @PostMapping("/{providerPaymentId}/fail")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark a payment as failed")
    public ResponseEntity<ApiResponse<String>> failPayment(@PathVariable String providerPaymentId,
                                                            @RequestParam String reason) {
        paymentService.failPayment(providerPaymentId, reason);
        return ResponseEntity.ok(ApiResponse.success("Payment marked as failed", providerPaymentId));
    }

    @GetMapping("/{providerPaymentId}")
    @Operation(summary = "Get payment details by provider payment ID")
    public ResponseEntity<ApiResponse<?>> getPayment(@PathVariable String providerPaymentId) {
        var payment = paymentService.getPaymentByProviderPaymentId(providerPaymentId);
        return ResponseEntity.ok(ApiResponse.success("Payment found", payment));
    }
}
