package com.driftstay.payment.service;

import com.driftstay.booking.entity.Booking;
import com.driftstay.booking.entity.Payment;
import com.driftstay.booking.entity.Refund;
import com.driftstay.booking.service.cancellation.CancellationPolicyResolver;
import com.driftstay.common.enums.RefundStatus;
import com.driftstay.common.enums.TransactionStatus;
import com.driftstay.payment.dto.response.PaymentResponse;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for processing refunds through the payment gateway.
 * Calculates the refund amount using the cancellation policy,
 * then processes it through the appropriate gateway.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {

    private final PaymentFactory paymentFactory;
    private final CancellationPolicyResolver cancellationPolicyResolver;
    private final WebhookService webhookService;
    private final EntityManager entityManager;

    /**
     * Process a full or partial refund for a payment.
     *
     * @param paymentId The payment to refund
     * @param amount    The amount to refund
     * @param reason    The reason for the refund
     * @return PaymentGateway response
     */
    @Transactional
    public PaymentResponse processRefund(Long paymentId, BigDecimal amount, String reason) {
        Payment payment = entityManager.find(Payment.class, paymentId);
        if (payment == null) {
            throw new IllegalArgumentException("Payment not found: " + paymentId);
        }

        PaymentGateway gateway = paymentFactory.getGateway(payment.getProvider());
        
        PaymentResponse gatewayResponse = gateway.processRefund(
                payment.getProviderPaymentId(), amount, reason
        );

        // Create refund record
        Refund refund = new Refund();
        refund.setPayment(payment);
        refund.setRefundReference("REF-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        refund.setAmount(amount);
        refund.setReason(reason);
        refund.setStatus(gatewayResponse.isSuccess() ? RefundStatus.PROCESSED : RefundStatus.FAILED);
        if (gatewayResponse.isSuccess()) {
            refund.setProcessedAt(LocalDateTime.now());
            payment.setStatus(TransactionStatus.REFUNDED);
            entityManager.merge(payment);
        }
        entityManager.persist(refund);

        log.info("Refund processed for payment {}: amount={}, status={}", 
                paymentId, amount, refund.getStatus());

        return gatewayResponse;
    }

    /**
     * Calculate and process a refund for a cancelled booking.
     */
    @Transactional
    public BigDecimal processCancellationRefund(Booking booking) {
        BigDecimal refundAmount = cancellationPolicyResolver.calculateRefund(
                booking, LocalDateTime.now()
        );

        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("No refund due for booking: {}", booking.getBookingReference());
            return BigDecimal.ZERO;
        }

        // Find the payment for this booking
        Payment payment = entityManager.createQuery(
                        "SELECT p FROM Payment p WHERE p.booking.id = :bookingId ORDER BY p.createdAt DESC",
                        Payment.class)
                .setParameter("bookingId", booking.getId())
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (payment != null) {
            processRefund(payment.getId(), refundAmount, 
                    "Cancellation refund for booking " + booking.getBookingReference());
        }

        return refundAmount;
    }
}
