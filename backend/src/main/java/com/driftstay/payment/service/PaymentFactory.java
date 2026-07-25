package com.driftstay.payment.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory for resolving payment gateway implementations.
 * When a new gateway is added (e.g., RazorpayGateway, StripeGateway),
 * it will be automatically discovered and available through this factory.
 */
@Component
public class PaymentFactory {

    private final Map<String, PaymentGateway> gatewayMap;

    /**
     * Constructor-based injection of all PaymentGateway beans into a map.
     */
    public PaymentFactory(List<PaymentGateway> gateways) {
        this.gatewayMap = gateways.stream()
                .collect(Collectors.toMap(
                        PaymentGateway::getProviderName,
                        Function.identity(),
                        (existing, replacement) -> {
                            // If there's a conflict (e.g., both DummyGateway and RazorpayGateway),
                            // prefer the non-dummy one
                            if ("DUMMY".equals(existing.getProviderName())) {
                                return replacement;
                            }
                            return existing;
                        }
                ));
    }

    /**
     * Get the appropriate payment gateway by provider name.
     *
     * @param providerName The provider name (e.g., "DUMMY", "RAZORPAY", "STRIPE")
     * @return The payment gateway implementation
     * @throws IllegalArgumentException if no gateway is found for the given provider
     */
    public PaymentGateway getGateway(String providerName) {
        PaymentGateway gateway = gatewayMap.get(providerName.toUpperCase());
        if (gateway == null) {
            throw new IllegalArgumentException(
                    "No payment gateway found for provider: " + providerName +
                            ". Available providers: " + gatewayMap.keySet()
            );
        }
        return gateway;
    }

    /**
     * Get the default gateway (the first one registered, typically DummyGateway).
     */
    public PaymentGateway getDefaultGateway() {
        return gatewayMap.values().iterator().next();
    }
}
