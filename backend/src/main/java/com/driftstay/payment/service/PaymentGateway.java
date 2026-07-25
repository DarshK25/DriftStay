package com.driftstay.payment.service;

import com.driftstay.payment.dto.request.PaymentRequest;
import com.driftstay.payment.dto.response.PaymentResponse;

import java.math.BigDecimal;

/**
 * Strategy interface for payment gateway integration.
 * Implementations: DummyGateway, RazorpayGateway, StripeGateway
 * New gateways can be plugged in without changing business logic.
 */
public interface PaymentGateway {

    /**
     * The name/identifier of this gateway provider.
     */
    String getProviderName();

    /**
     * Initiate a payment with the gateway.
     *
     * @param request Payment details
     * @return Response from the gateway
     */
    PaymentResponse initiatePayment(PaymentRequest request);

    /**
     * Process a refund through the gateway.
     *
     * @param providerPaymentId The gateway's payment ID
     * @param amount            Amount to refund
     * @param reason            Reason for refund
     * @return Gateway response
     */
    PaymentResponse processRefund(String providerPaymentId, BigDecimal amount, String reason);

    /**
     * Verify the status of a payment with the gateway.
     *
     * @param providerPaymentId The gateway's payment ID
     * @return Current payment status from gateway
     */
    PaymentResponse verifyPayment(String providerPaymentId);

    /**
     * Verify a webhook signature to ensure it came from the gateway.
     *
     * @param payload       The raw webhook payload
     * @param signature     The signature header value
     * @param webhookSecret The shared secret
     * @return true if the signature is valid
     */
    boolean verifyWebhookSignature(String payload, String signature, String webhookSecret);
}
