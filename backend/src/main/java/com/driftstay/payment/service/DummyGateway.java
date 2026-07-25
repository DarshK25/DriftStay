package com.driftstay.payment.service;

import com.driftstay.payment.dto.request.PaymentRequest;
import com.driftstay.payment.dto.response.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A dummy/mock payment gateway for development and testing.
 * Simulates payment flow without real payment processing.
 * Later, replace this with RazorpayGateway or StripeGateway.
 */
@Slf4j
@Component
public class DummyGateway implements PaymentGateway {

    @Override
    public String getProviderName() {
        return "DUMMY";
    }

    @Override
    public PaymentResponse initiatePayment(PaymentRequest request) {
        log.info("DUMMY GATEWAY: Initiating payment for booking {} of amount {} {}",
                request.getBookingId(), request.getAmount(), request.getCurrency());

        // Simulate payment processing delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate successful payment
        String paymentId = "dummy_pay_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String orderId = "dummy_ord_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        log.info("DUMMY GATEWAY: Payment successful. Payment ID: {}", paymentId);

        return PaymentResponse.builder()
                .success(true)
                .providerPaymentId(paymentId)
                .providerOrderId(orderId)
                .transactionReference("TXN" + System.currentTimeMillis())
                .status("SUCCESS")
                .message("Payment processed successfully (DUMMY)")
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .gatewayResponse("{\"simulated\": true, \"mode\": \"dummy\"}")
                .build();
    }

    @Override
    public PaymentResponse processRefund(String providerPaymentId, BigDecimal amount, String reason) {
        log.info("DUMMY GATEWAY: Processing refund of {} for payment {}", amount, providerPaymentId);

        String refundId = "dummy_ref_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        return PaymentResponse.builder()
                .success(true)
                .providerPaymentId(providerPaymentId)
                .transactionReference(refundId)
                .status("REFUNDED")
                .message("Refund processed successfully (DUMMY)")
                .amount(amount)
                .gatewayResponse("{\"simulated\": true, \"refund\": true}")
                .build();
    }

    @Override
    public PaymentResponse verifyPayment(String providerPaymentId) {
        log.debug("DUMMY GATEWAY: Verifying payment {}", providerPaymentId);

        return PaymentResponse.builder()
                .success(true)
                .providerPaymentId(providerPaymentId)
                .status("SUCCESS")
                .message("Payment verified (DUMMY)")
                .build();
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature, String webhookSecret) {
        // Dummy gateway always trusts webhooks
        return true;
    }
}
