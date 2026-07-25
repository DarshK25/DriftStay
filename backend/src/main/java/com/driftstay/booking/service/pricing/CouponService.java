package com.driftstay.booking.service.pricing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;

/**
 * Coupon service for validating and applying coupon codes.
 * Currently uses an in-memory map. Production version would use a database table.
 */
@Slf4j
@Service
public class CouponService {

    private static final Map<String, CouponDefinition> KNOWN_COUPONS = Map.of(
            "WELCOME10", new CouponDefinition("WELCOME10", "First booking 10% off", 
                    BigDecimal.valueOf(10), DiscountType.PERCENTAGE, null),
            "SAVE15", new CouponDefinition("SAVE15", "Save 15% on your stay", 
                    BigDecimal.valueOf(15), DiscountType.PERCENTAGE, null),
            "DRIFT20", new CouponDefinition("DRIFT20", "Special 20% discount", 
                    BigDecimal.valueOf(20), DiscountType.PERCENTAGE, null),
            "FLAT500", new CouponDefinition("FLAT500", "Flat ₹500 off", 
                    BigDecimal.valueOf(500), DiscountType.FLAT_AMOUNT, BigDecimal.valueOf(3000))
    );

    /**
     * Validate a coupon code and return the discount amount.
     *
     * @param couponCode The coupon code to validate
     * @param subtotal   The booking subtotal before discount
     * @return Optional containing the discount amount, or empty if invalid
     */
    public Optional<BigDecimal> validateAndApply(String couponCode, BigDecimal subtotal) {
        if (couponCode == null || couponCode.isBlank()) {
            return Optional.empty();
        }

        CouponDefinition coupon = KNOWN_COUPONS.get(couponCode.toUpperCase().trim());
        if (coupon == null) {
            log.warn("Unknown coupon code: {}", couponCode);
            return Optional.empty();
        }

        // Check minimum amount requirement
        if (coupon.minAmount != null && subtotal.compareTo(coupon.minAmount) < 0) {
            log.warn("Coupon {} requires minimum amount of {}, but subtotal is {}",
                    couponCode, coupon.minAmount, subtotal);
            return Optional.empty();
        }

        BigDecimal discount = switch (coupon.type) {
            case PERCENTAGE -> subtotal.multiply(coupon.value.divide(BigDecimal.valueOf(100)))
                    .setScale(2, RoundingMode.HALF_UP);
            case FLAT_AMOUNT -> coupon.value;
        };

        log.info("Coupon {} applied: discount={}", couponCode, discount);
        return Optional.of(discount);
    }

    private record CouponDefinition(
            String code,
            String description,
            BigDecimal value,
            DiscountType type,
            BigDecimal minAmount
    ) {}

    private enum DiscountType {
        PERCENTAGE,
        FLAT_AMOUNT
    }
}
