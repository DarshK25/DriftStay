package com.driftstay.booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceBreakdown {

    private BigDecimal basePrice;
    private BigDecimal weekendSurcharge;
    private BigDecimal holidaySurcharge;
    private BigDecimal cleaningFee;
    private BigDecimal discount;
    private BigDecimal couponDiscount;
    private BigDecimal subtotal;
    private BigDecimal gst;
    private BigDecimal convenienceFee;
    private BigDecimal totalAmount;

    public PriceBreakdown calculate() {
        // Base + surcharges
        BigDecimal beforeDiscount = basePrice
                .add(weekendSurcharge != null ? weekendSurcharge : BigDecimal.ZERO)
                .add(holidaySurcharge != null ? holidaySurcharge : BigDecimal.ZERO);

        // Apply discounts on the subtotal before fees
        BigDecimal totalDiscount = (discount != null ? discount : BigDecimal.ZERO)
                .add(couponDiscount != null ? couponDiscount : BigDecimal.ZERO);

        subtotal = beforeDiscount.subtract(totalDiscount);

        // GST (12% on subtotal for hotels in India)
        gst = subtotal.multiply(BigDecimal.valueOf(0.12))
                .setScale(2, RoundingMode.HALF_UP);

        // Convenience fee (2% on subtotal)
        convenienceFee = subtotal.multiply(BigDecimal.valueOf(0.02))
                .setScale(2, RoundingMode.HALF_UP);

        // Add fees
        BigDecimal totalFees = cleaningFee != null ? cleaningFee : BigDecimal.ZERO;
        totalAmount = subtotal.add(gst).add(convenienceFee).add(totalFees)
                .setScale(2, RoundingMode.HALF_UP);

        return this;
    }
}
