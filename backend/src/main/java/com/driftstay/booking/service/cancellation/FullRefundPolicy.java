package com.driftstay.booking.service.cancellation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Full refund policy.
 * Refunds 100% if cancelled at least 48 hours before check-in.
 * After that, no refund.
 */
@Slf4j
@Component
public class FullRefundPolicy implements CancellationPolicy {

    private static final long FREE_CANCELLATION_HOURS = 48;

    @Override
    public String getPolicyName() {
        return "FULL_REFUND";
    }

    @Override
    public BigDecimal calculateRefund(BigDecimal totalAmount,
                                      LocalDateTime cancellationTime,
                                      LocalDateTime checkInTime,
                                      LocalDateTime bookingCreatedTime) {
        long hoursUntilCheckIn = ChronoUnit.HOURS.between(cancellationTime, checkInTime);

        if (hoursUntilCheckIn >= FREE_CANCELLATION_HOURS) {
            // Full refund
            log.debug("Full refund: cancelled {} hours before check-in (threshold: {}h)",
                    hoursUntilCheckIn, FREE_CANCELLATION_HOURS);
            return totalAmount;
        }

        // No refund after the free cancellation window has passed
        log.debug("No refund: cancelled only {} hours before check-in", hoursUntilCheckIn);
        return BigDecimal.ZERO;
    }

    @Override
    public String getDescription() {
        return String.format("Free cancellation up to %d hours before check-in. Full refund.", FREE_CANCELLATION_HOURS);
    }
}
