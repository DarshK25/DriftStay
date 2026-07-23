package com.driftstay.payment.controller;

import com.driftstay.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing endpoints")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{providerPaymentId}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Confirm a payment")
    public ResponseEntity<Map<String, String>> confirmPayment(@PathVariable String providerPaymentId) {
        paymentService.confirmPayment(providerPaymentId);
        return ResponseEntity.ok(Map.of("status", "confirmed"));
    }

    @PostMapping("/{providerPaymentId}/fail")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark a payment as failed")
    public ResponseEntity<Map<String, String>> failPayment(@PathVariable String providerPaymentId,
                                                            @RequestParam String reason) {
        paymentService.failPayment(providerPaymentId, reason);
        return ResponseEntity.ok(Map.of("status", "failed", "reason", reason));
    }

    @GetMapping("/{providerPaymentId}")
    @Operation(summary = "Get payment details by provider payment ID")
    public ResponseEntity<?> getPayment(@PathVariable String providerPaymentId) {
        var payment = paymentService.getPaymentByProviderPaymentId(providerPaymentId);
        return ResponseEntity.ok(payment);
    }
}
