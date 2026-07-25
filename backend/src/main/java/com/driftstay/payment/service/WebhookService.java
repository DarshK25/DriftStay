package com.driftstay.payment.service;

import com.driftstay.booking.entity.Payment;
import com.driftstay.booking.entity.PaymentTransaction;
import com.driftstay.booking.repository.BookingRepository;
import com.driftstay.common.enums.TransactionStatus;
import com.driftstay.payment.dto.response.PaymentResponse;
import com.driftstay.payment.service.PaymentFactory;
import com.driftstay.payment.service.PaymentGateway;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Handles incoming webhook requests from payment gateways.
 * Verifies signatures and updates payment status accordingly.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final PaymentFactory paymentFactory;
    private final BookingRepository bookingRepository;
    private final EntityManager entityManager;

    /**
     * Process a webhook event from a payment gateway.
     *
     * @param provider      The gateway provider name
     * @param payload       The raw webhook payload
     * @param signature     The webhook signature header
     * @param webhookSecret The shared webhook secret
     * @return true if the webhook was processed successfully
     */
    @Transactional
    public boolean processWebhook(String provider, String payload, String signature, String webhookSecret) {
        PaymentGateway gateway = paymentFactory.getGateway(provider);

        // Verify the webhook signature
        if (!gateway.verifyWebhookSignature(payload, signature, webhookSecret)) {
            log.warn("Webhook signature verification failed for provider: {}", provider);
            return false;
        }

        // Parse the webhook event and update payment status
        log.info("Webhook received and verified from provider: {}", provider);

        // The actual webhook parsing logic will depend on the specific gateway
        // This is a placeholder that will be implemented when integrating real gateways

        return true;
    }

    /**
     * Log a gateway transaction for audit trail.
     */
    @Transactional
    public PaymentTransaction logGatewayTransaction(Payment payment, String gateway,
                                                     String gatewayTransactionId, String gatewayResponse,
                                                     TransactionStatus status) {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setPayment(payment);
        transaction.setGateway(gateway);
        transaction.setGatewayTransactionId(gatewayTransactionId);
        transaction.setGatewayResponse(gatewayResponse);
        transaction.setStatus(status);

        entityManager.persist(transaction);

        log.debug("Gateway transaction logged: paymentId={}, gateway={}, txnId={}, status={}",
                payment.getId(), gateway, gatewayTransactionId, status);

        return transaction;
    }
}
