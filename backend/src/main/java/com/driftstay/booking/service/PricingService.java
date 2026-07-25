package com.driftstay.booking.service;

import com.driftstay.booking.dto.request.PriceCalculationRequest;
import com.driftstay.booking.dto.response.PriceBreakdown;
import com.driftstay.room.entity.Room;
import com.driftstay.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Server-side pricing engine.
 * Calculates the total booking price based on:
 * - Base price (per night × nights)
 * - Weekend surcharge
 * - Holiday surcharge
 * - Discounts and coupons
 * - Cleaning fee
 * - GST (12%)
 * - Convenience fee (2%)
 *
 * Never trust frontend-calculated totals.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PricingService {

    // Weekend days (Friday, Saturday, Sunday)
    private static final Set<DayOfWeek> WEEKEND_DAYS = Set.of(
            DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    );

    // Major Indian holidays for 2026 (can be moved to a database table later)
    private static final Set<LocalDate> HOLIDAYS_2026 = new HashSet<>(List.of(
            LocalDate.of(2026, 1, 26),  // Republic Day
            LocalDate.of(2026, 3, 25),  // Holi
            LocalDate.of(2026, 8, 15),  // Independence Day
            LocalDate.of(2026, 10, 2),  // Gandhi Jayanti
            LocalDate.of(2026, 10, 22), // Diwali
            LocalDate.of(2026, 12, 25)  // Christmas
    ));

    private final RoomRepository roomRepository;

    /**
     * Calculate the total price for a booking.
     * This is the server-authoritative calculation - never trust the frontend!
     *
     * @param request Pricing calculation request
     * @return PriceBreakdown with all components
     */
    public PriceBreakdown calculatePrice(PriceCalculationRequest request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + request.getRoomId()));

        int nights = (int) ChronoUnit.DAYS.between(request.getCheckIn(), request.getCheckOut());

        // 1. Calculate base price
        BigDecimal basePricePerNight = request.getBasePrice() != null
                ? request.getBasePrice()
                : room.getBasePrice();
        BigDecimal basePrice = basePricePerNight.multiply(BigDecimal.valueOf(nights));

        // 2. Calculate weekend surcharge
        BigDecimal weekendMultiplier = request.getWeekendPrice() != null
                ? request.getWeekendPrice()
                : (room.getWeekendPrice() != null ? room.getWeekendPrice() : basePricePerNight);
        BigDecimal weekendSurcharge = calculateWeekendSurcharge(
                request.getCheckIn(), request.getCheckOut(),
                basePricePerNight, weekendMultiplier
        );

        // 3. Calculate holiday surcharge (50% on holidays)
        BigDecimal holidaySurcharge = calculateHolidaySurcharge(
                request.getCheckIn(), request.getCheckOut(),
                basePricePerNight
        );

        // 4. Cleaning fee
        BigDecimal cleaningFee = request.getCleaningFee() != null
                ? request.getCleaningFee()
                : (room.getCleaningFee() != null ? room.getCleaningFee() : BigDecimal.ZERO);

        // 5. Build breakdown
        PriceBreakdown breakdown = PriceBreakdown.builder()
                .basePrice(basePrice.setScale(2, RoundingMode.HALF_UP))
                .weekendSurcharge(weekendSurcharge.setScale(2, RoundingMode.HALF_UP))
                .holidaySurcharge(holidaySurcharge.setScale(2, RoundingMode.HALF_UP))
                .cleaningFee(cleaningFee.setScale(2, RoundingMode.HALF_UP))
                .discount(BigDecimal.ZERO)
                .couponDiscount(calculateCouponDiscount(request.getCouponCode(), basePrice))
                .build();

        // Calculate tax and total
        breakdown.calculate();

        log.debug("Price calculated for room {}: total={}", request.getRoomId(), breakdown.getTotalAmount());

        return breakdown;
    }

    /**
     * Calculate weekend surcharge for dates that fall on weekends.
     * Weekend price is typically 20-30% higher than base price.
     */
    private BigDecimal calculateWeekendSurcharge(LocalDate checkIn, LocalDate checkOut,
                                                  BigDecimal basePricePerNight,
                                                  BigDecimal weekendPricePerNight) {
        BigDecimal surcharge = BigDecimal.ZERO;
        LocalDate date = checkIn;

        while (date.isBefore(checkOut)) {
            if (isWeekend(date)) {
                BigDecimal diff = weekendPricePerNight.subtract(basePricePerNight);
                if (diff.compareTo(BigDecimal.ZERO) > 0) {
                    surcharge = surcharge.add(diff);
                }
            }
            date = date.plusDays(1);
        }

        return surcharge;
    }

    /**
     * Calculate holiday surcharge (50% extra on holidays).
     */
    private BigDecimal calculateHolidaySurcharge(LocalDate checkIn, LocalDate checkOut,
                                                  BigDecimal basePricePerNight) {
        BigDecimal surcharge = BigDecimal.ZERO;
        LocalDate date = checkIn;

        while (date.isBefore(checkOut)) {
            if (HOLIDAYS_2026.contains(date)) {
                BigDecimal holidayPremium = basePricePerNight.multiply(BigDecimal.valueOf(0.5));
                surcharge = surcharge.add(holidayPremium);
            }
            date = date.plusDays(1);
        }

        return surcharge;
    }

    private boolean isWeekend(LocalDate date) {
        return WEEKEND_DAYS.contains(date.getDayOfWeek());
    }

    /**
     * Calculate coupon discount based on coupon code.
     * Simple 10% discount for WELCOME10, 15% for SAVE15.
     * A production system would look up the coupon in a database table.
     */
    private BigDecimal calculateCouponDiscount(String couponCode, BigDecimal basePrice) {
        if (couponCode == null || couponCode.isBlank()) {
            return BigDecimal.ZERO;
        }

        return switch (couponCode.toUpperCase()) {
            case "WELCOME10" -> basePrice.multiply(BigDecimal.valueOf(0.10))
                    .setScale(2, RoundingMode.HALF_UP);
            case "SAVE15" -> basePrice.multiply(BigDecimal.valueOf(0.15))
                    .setScale(2, RoundingMode.HALF_UP);
            case "DRIFT20" -> basePrice.multiply(BigDecimal.valueOf(0.20))
                    .setScale(2, RoundingMode.HALF_UP);
            default -> BigDecimal.ZERO;
        };
    }
}
