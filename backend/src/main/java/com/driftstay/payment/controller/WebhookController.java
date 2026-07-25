package com.driftstay.payment.controller;

import com.driftstay.payment.service.WebhookService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;

/**
 * Handles incoming webhook requests from payment gateways.
 * These endpoints are publicly accessible (no auth) because
 * gateways need to call them. Security is via signature verification.
 */
@Slf4j
@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    /**
     * Generic webhook handler for all payment gateways.
     * POST /api/webhooks/payment/{provider}
     */
    @PostMapping("/payment/{provider}")
    public ResponseEntity<String> handlePaymentWebhook(
            @PathVariable String provider,
            @RequestBody String payload,
            @RequestHeader("X-Webhook-Signature") String signature,
            @RequestHeader(value = "X-Webhook-Secret", required = false) String secret,
            HttpServletRequest request) {

        log.info("Webhook received from provider: {}", provider);

        boolean processed = webhookService.processWebhook(
                provider, payload, signature, secret
        );

        if (processed) {
            return ResponseEntity.ok("Webhook processed successfully");
        } else {
            return ResponseEntity.status(400).body("Webhook processing failed");
        }
    }
}
