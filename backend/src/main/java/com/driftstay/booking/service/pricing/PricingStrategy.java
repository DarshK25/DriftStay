package com.driftstay.booking.service.pricing;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Strategy pattern for pricing calculations.
 * Each implementation handles a specific pricing component:
 * - BasePriceStrategy: Base price × nights
 * - WeekendSurchargeStrategy: Weekend price delta
 * - HolidaySurchargeStrategy: Holiday premium
 * - CleaningFeeStrategy: One-time cleaning fee
 * - PlatformFeeStrategy: Platform/convenience fee
 * - DiscountStrategy: Promotional discounts
 * - TaxStrategy: GST calculation
 * - CouponStrategy: Coupon-based discounts
 */
public interface PricingStrategy {

    /**
     * Unique name for this pricing strategy.
     */
    String getName();

    /**
     * Calculate the price component for the given booking parameters.
     *
     * @param basePricePerNight The room's base nightly rate
     * @param weekendPricePerNight The room's weekend nightly rate
     * @param checkIn Check-in date
     * @param checkOut Check-out date
     * @param guestCount Number of guests
     * @param couponCode Optional coupon code
     * @return The calculated price contribution
     */
    BigDecimal calculate(BigDecimal basePricePerNight,
                         BigDecimal weekendPricePerNight,
                         LocalDate checkIn,
                         LocalDate checkOut,
                         int guestCount,
                         String couponCode);
}
