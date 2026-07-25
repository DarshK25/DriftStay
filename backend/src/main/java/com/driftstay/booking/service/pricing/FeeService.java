package com.driftstay.booking.service.pricing;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service for calculating various fees:
 * - Platform/Convenience fee (2% of subtotal)
 * - Cleaning fee (fixed per stay, from room config)
 * - Service fee
 */
@Service
public class FeeService {

    private static final BigDecimal PLATFORM_FEE_RATE = BigDecimal.valueOf(0.02);
    private static final BigDecimal SERVICE_FEE_RATE = BigDecimal.valueOf(0.03);

    /**
     * Calculate the platform/convenience fee.
     */
    public BigDecimal calculatePlatformFee(BigDecimal subtotal) {
        return subtotal.multiply(PLATFORM_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate the service fee.
     */
    public BigDecimal calculateServiceFee(BigDecimal subtotal) {
        return subtotal.multiply(SERVICE_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate total fees (platform + service + cleaning).
     */
    public BigDecimal calculateTotalFees(BigDecimal subtotal, BigDecimal cleaningFee) {
        BigDecimal platformFee = calculatePlatformFee(subtotal);
        BigDecimal serviceFee = calculateServiceFee(subtotal);
        BigDecimal cleaning = cleaningFee != null ? cleaningFee : BigDecimal.ZERO;

        return platformFee.add(serviceFee).add(cleaning);
    }
}
