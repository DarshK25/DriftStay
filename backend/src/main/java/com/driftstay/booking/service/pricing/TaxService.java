package com.driftstay.booking.service.pricing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Tax calculation service.
 * Handles GST (Goods and Services Tax) for Indian hospitality:
 * - Hotels under ₹1,000: No GST
 * - Hotels ₹1,000 - ₹2,500: 12% GST
 * - Hotels above ₹2,500: 18% GST
 */
@Slf4j
@Service
public class TaxService {

    private static final BigDecimal GST_SLAB_1 = new BigDecimal("1000");
    private static final BigDecimal GST_SLAB_2 = new BigDecimal("2500");
    private static final BigDecimal GST_RATE_LOW = BigDecimal.valueOf(0.00);
    private static final BigDecimal GST_RATE_MEDIUM = BigDecimal.valueOf(0.12);
    private static final BigDecimal GST_RATE_HIGH = BigDecimal.valueOf(0.18);

    /**
     * Calculate GST on the given subtotal.
     *
     * @param subtotal The amount before tax
     * @return The GST amount
     */
    public BigDecimal calculateGst(BigDecimal subtotal) {
        BigDecimal gstRate = getGstRate(subtotal);
        BigDecimal gst = subtotal.multiply(gstRate).setScale(2, RoundingMode.HALF_UP);

        log.debug("GST calculated: subtotal={}, rate={}, gst={}", subtotal, gstRate, gst);
        return gst;
    }

    /**
     * Calculate the total including all taxes.
     */
    public BigDecimal calculateTotalWithTax(BigDecimal subtotal) {
        return subtotal.add(calculateGst(subtotal));
    }

    private BigDecimal getGstRate(BigDecimal amount) {
        if (amount.compareTo(GST_SLAB_1) < 0) return GST_RATE_LOW;
        if (amount.compareTo(GST_SLAB_2) <= 0) return GST_RATE_MEDIUM;
        return GST_RATE_HIGH;
    }
}
