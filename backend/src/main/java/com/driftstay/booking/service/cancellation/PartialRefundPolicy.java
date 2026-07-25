package com.driftstay.booking.service.cancellation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Partial refund policy.
 * - 100% refund if cancelled >= 48 hours before check-in
 * - 50% refund if cancelled between 24-48 hours before check-in
 * - No refund if cancelled < 24 hours before check-in
 */
@Slf4j
@Component
public class PartialRefundPolicy implements CancellationPolicy {

    private static final long FULL_REFUND_HOURS = 48;
    private static final long PARTIAL_REFUND_HOURS = 24;
    private static final BigDecimal HALF = BigDecimal.valueOf(0.5);

    @Override
    public String getPolicyName() {
        return "PARTIAL_REFUND";
    }

    @Override
    public BigDecimal calculateRefund(BigDecimal totalAmount,
                                      LocalDateTime cancellationTime,
                                      LocalDateTime checkInTime,
                                      LocalDateTime bookingCreatedTime) {
        long hoursUntilCheckIn = ChronoUnit.HOURS.between(cancellationTime, checkInTime);

        if (hoursUntilCheckIn >= FULL_REFUND_HOURS) {
            // Full refund
            log.debug("PartialRefundPolicy: Full refund ({}h before check-in)", hoursUntilCheckIn);
            return totalAmount;
        }

        if (hoursUntilCheckIn >= PARTIAL_REFUND_HOURS) {
            // 50% refund
            BigDecimal refund = totalAmount.multiply(HALF)
                    .setScale(2, RoundingMode.HALF_UP);
            log.debug("PartialRefundPolicy: 50% refund = {} ({}h before check-in)", refund, hoursUntilCheckIn);
            return refund;
        }

        // No refund
        log.debug("PartialRefundPolicy: No refund (only {}h before check-in)", hoursUntilCheckIn);
        return BigDecimal.ZERO;
    }

    @Override
    public String getDescription() {
        return "Full refund 48+ hours before check-in. 50% refund 24-48 hours before. No refund within 24 hours.";
    }
}
