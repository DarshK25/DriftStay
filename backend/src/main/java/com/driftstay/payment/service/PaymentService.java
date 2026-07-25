package com.driftstay.payment.service;

import com.driftstay.booking.entity.Booking;
import com.driftstay.booking.entity.Payment;
import com.driftstay.booking.event.BookingEventPublisher;
import com.driftstay.booking.repository.BookingRepository;
import com.driftstay.common.enums.TransactionStatus;
import com.driftstay.payment.dto.request.PaymentRequest;
import com.driftstay.payment.dto.response.PaymentResponse;
import com.driftstay.payment.entity.IdempotencyKey;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Core payment processing service with idempotency and resilience support.
 *
 * Idempotency Flow:
 * 1. Client sends Idempotency-Key header
 * 2. If key exists and COMPLETED → return cached response (no double charge)
 * 3. If key exists and IN_PROGRESS → return 409 (still processing)
 * 4. If new → create IN_PROGRESS, process payment, mark COMPLETED
 *
 * Resilience:
 * - @Retry: retries on gateway timeouts and 5xx errors (up to 3 times)
 * - @CircuitBreaker: opens circuit after 5 failures, half-opens after 30s
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentFactory paymentFactory;
    private final BookingEventPublisher eventPublisher;
    private final WebhookService webhookService;
    private final IdempotencyService idempotencyService;
    private final EntityManager entityManager;

    /**
     * Process a payment with idempotency support.
     * If an Idempotency-Key is provided, duplicate requests return cached response.
     */
    @Transactional
    @Retry(name = "paymentGateway", fallbackMethod = "paymentFallback")
    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "paymentFallback")
    public PaymentResponse processPayment(Long bookingId, BigDecimal amount,
                                          String currency, String paymentMethod,
                                          String provider, String idempotencyKey) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        // Check idempotency (prevents double charges)
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<IdempotencyKey> existing = idempotencyService.tryAcquire(
                    idempotencyKey, "PAYMENT", bookingId);

            if (existing.isPresent()) {
                IdempotencyKey key = existing.get();

                if (key.getStatus() == IdempotencyKey.IdempotencyStatus.COMPLETED) {
                    log.info("Returning cached payment response for idempotency key: {}", idempotencyKey);
                    return PaymentResponse.builder()
                            .success(true)
                            .transactionReference(key.getResponseBody())
                            .status("COMPLETED")
                            .message("Payment already processed (idempotent)")
                            .amount(amount)
                            .currency(currency != null ? currency : "INR")
                            .build();
                }

                if (key.getStatus() == IdempotencyKey.IdempotencyStatus.IN_PROGRESS) {
                    throw new IllegalStateException(
                            "Payment already in progress for idempotency key: " + idempotencyKey);
                }
            }
        }

        // Build payment request
        PaymentRequest request = PaymentRequest.builder()
                .bookingId(bookingId)
                .amount(amount)
                .currency(currency != null ? currency : "INR")
                .paymentMethod(paymentMethod)
                .provider(provider != null ? provider : "DUMMY")
                .description("Payment for booking " + booking.getBookingReference())
                .build();

        // Get the appropriate gateway and process payment
        PaymentGateway gateway = paymentFactory.getGateway(request.getProvider());
        PaymentResponse gatewayResponse = gateway.initiatePayment(request);

        // Persist payment record
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setCurrency(request.getCurrency());
        payment.setPaymentMethod(paymentMethod);
        payment.setProvider(gateway.getProviderName());
        payment.setProviderPaymentId(gatewayResponse.getProviderPaymentId());
        payment.setProviderOrderId(gatewayResponse.getProviderOrderId());
        payment.setTransactionReference(gatewayResponse.getTransactionReference());
        payment.setStatus(gatewayResponse.isSuccess() ? TransactionStatus.SUCCESS : TransactionStatus.FAILED);
        if (gatewayResponse.isSuccess()) {
            payment.setPaidAt(LocalDateTime.now());
        }
        entityManager.persist(payment);

        // Log the gateway transaction
        webhookService.logGatewayTransaction(
                payment,
                gateway.getProviderName(),
                gatewayResponse.getProviderPaymentId(),
                gatewayResponse.getGatewayResponse(),
                gatewayResponse.isSuccess() ? TransactionStatus.SUCCESS : TransactionStatus.FAILED
        );

        // Mark idempotency key as completed
        if (idempotencyKey != null && !idempotencyKey.isBlank() && gatewayResponse.isSuccess()) {
            idempotencyService.complete(idempotencyKey, gatewayResponse.getTransactionReference(), 200);
        }

        log.info("Payment processed for booking {}: {} {} via {} - {}",
                booking.getBookingReference(), amount, currency, provider,
                gatewayResponse.isSuccess() ? "SUCCESS" : "FAILED");

        return gatewayResponse;
    }

    /**
     * Fallback method when payment gateway retries and circuit breaker are exhausted.
     */
    public PaymentResponse paymentFallback(Long bookingId, BigDecimal amount,
                                           String currency, String paymentMethod,
                                           String provider, String idempotencyKey,
                                           Throwable t) {
        log.error("Payment failed after all retries for booking {}: {}",
                bookingId, t.getMessage());

        return PaymentResponse.builder()
                .success(false)
                .status("FAILED")
                .message("Payment processing failed after multiple attempts: " + t.getMessage())
                .amount(amount)
                .currency(currency != null ? currency : "INR")
                .build();
    }

    /**
     * Process a refund for a payment.
     */
    @Transactional
    public PaymentResponse processRefund(Long paymentId, BigDecimal amount, String reason) {
        Payment payment = entityManager.find(Payment.class, paymentId);
        if (payment == null) {
            throw new IllegalArgumentException("Payment not found: " + paymentId);
        }

        PaymentGateway gateway = paymentFactory.getGateway(payment.getProvider());
        PaymentResponse response = gateway.processRefund(
                payment.getProviderPaymentId(), amount, reason
        );

        if (response.isSuccess()) {
            payment.setStatus(TransactionStatus.REFUNDED);
            entityManager.merge(payment);
        }

        log.info("Refund processed for payment {}: {} - {}",
                paymentId, amount, response.isSuccess() ? "SUCCESS" : "FAILED");

        return response;
    }
}
