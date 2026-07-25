package com.driftstay.booking.service.cancellation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * No refund policy.
 * Used for promotional/non-refundable bookings or last-minute cancellations.
 */
@Slf4j
@Component
public class NoRefundPolicy implements CancellationPolicy {

    @Override
    public String getPolicyName() {
        return "NO_REFUND";
    }

    @Override
    public BigDecimal calculateRefund(BigDecimal totalAmount,
                                      LocalDateTime cancellationTime,
                                      LocalDateTime checkInTime,
                                      LocalDateTime bookingCreatedTime) {
        log.debug("NoRefundPolicy: No refund applicable");
        return BigDecimal.ZERO;
    }

    @Override
    public String getDescription() {
        return "Non-refundable booking. No cancellation refund available.";
    }
}
