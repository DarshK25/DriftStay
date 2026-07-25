package com.driftstay.booking.service.cancellation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Strategy interface for cancellation/refund policies.
 *
 * Implementations define different refund calculation strategies:
 * - FullRefundPolicy: 100% refund if cancelled within free cancellation window
 * - PartialRefundPolicy: 50% refund if cancelled after free window but before a cutoff
 * - NoRefundPolicy: No refund if cancelled too late or is a non-refundable booking
 * - TwentyFourHourPolicy: Full refund within 24 hours of booking
 * - FortyEightHourPolicy: Full refund within 48 hours of check-in
 */
public interface CancellationPolicy {

    /**
     * The name/identifier of this policy.
     */
    String getPolicyName();

    /**
     * Calculate the refund amount based on the cancellation timing.
     *
     * @param totalAmount         The total booking amount
     * @param cancellationTime    When the cancellation is requested
     * @param checkInTime         The scheduled check-in time
     * @param bookingCreatedTime  When the booking was created
     * @return Refund amount (may be zero)
     */
    BigDecimal calculateRefund(BigDecimal totalAmount,
                               LocalDateTime cancellationTime,
                               LocalDateTime checkInTime,
                               LocalDateTime bookingCreatedTime);

    /**
     * Returns a description of this policy for display to users.
     */
    String getDescription();
}
