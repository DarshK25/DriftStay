package com.driftstay.booking.service.cancellation;

import com.driftstay.booking.entity.Booking;
import com.driftstay.common.enums.BookingStatus;
import com.driftstay.property.entity.PropertyPolicy;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Resolves the appropriate CancellationPolicy and calculates refunds.
 * Uses property-level policy settings (freeCancellationHours) to determine
 * which strategy to apply.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CancellationPolicyResolver {

    private final FullRefundPolicy fullRefundPolicy;
    private final PartialRefundPolicy partialRefundPolicy;
    private final NoRefundPolicy noRefundPolicy;
    private final EntityManager entityManager;

    /**
     * Calculate the refund amount for a booking cancellation.
     *
     * @param booking          The booking being cancelled
     * @param cancellationTime When the cancellation is requested
     * @return The calculated refund amount
     */
    public BigDecimal calculateRefund(Booking booking, LocalDateTime cancellationTime) {
        // Determine the appropriate policy
        CancellationPolicy policy = resolvePolicy(booking);

        // Calculate check-in time (combine check-in date with property's check-in time)
        LocalDateTime checkInTime = booking.getCheckIn().atTime(LocalTime.of(14, 0)); // Default 2 PM

        // Try to get the actual check-in time from the property policy
        try {
            String query = "SELECT pp.checkInTime FROM PropertyPolicy pp WHERE pp.property.id = :propertyId";
            LocalTime policyCheckIn = entityManager.createQuery(query, LocalTime.class)
                    .setParameter("propertyId", booking.getPropertyId())
                    .getSingleResult();
            if (policyCheckIn != null) {
                checkInTime = booking.getCheckIn().atTime(policyCheckIn);
            }
        } catch (Exception e) {
            // Use default check-in time
            log.debug("Could not fetch property policy for booking {}, using default check-in time", 
                    booking.getBookingReference());
        }

        BigDecimal refund = policy.calculateRefund(
                booking.getTotalAmount(),
                cancellationTime,
                checkInTime,
                booking.getCreatedAt()
        );

        log.info("Refund calculated for booking {}: {} (policy: {})",
                booking.getBookingReference(), refund, policy.getPolicyName());

        return refund;
    }

    /**
     * Resolve the appropriate cancellation policy based on the booking.
     */
    private CancellationPolicy resolvePolicy(Booking booking) {
        // If already checked in, no refund
        if (booking.getBookingStatus() == BookingStatus.CHECKED_IN) {
            return noRefundPolicy;
        }

        // Try to use property-level policy if available
        try {
            String query = "SELECT pp.freeCancellationHours FROM PropertyPolicy pp WHERE pp.property.id = :propertyId";
            Integer freeCancellationHours = entityManager.createQuery(query, Integer.class)
                    .setParameter("propertyId", booking.getPropertyId())
                    .getSingleResult();

            if (freeCancellationHours != null) {
                // If free cancellation hours is high (>= 48), use full refund policy
                if (freeCancellationHours >= 48) {
                    return fullRefundPolicy;
                }
                // If moderate (24-47), use partial refund policy
                if (freeCancellationHours >= 24) {
                    return partialRefundPolicy;
                }
                // If low (< 24), no refund
                return noRefundPolicy;
            }
        } catch (Exception e) {
            log.debug("No property policy found for booking {}, using default full refund policy",
                    booking.getBookingReference());
        }

        // Default to full refund policy
        return fullRefundPolicy;
    }

    /**
     * Get the policy description for a property (for display purposes).
     */
    public String getPolicyDescription(Long propertyId) {
        try {
            String query = "SELECT pp.freeCancellationHours FROM PropertyPolicy pp WHERE pp.property.id = :propertyId";
            Integer freeCancellationHours = entityManager.createQuery(query, Integer.class)
                    .setParameter("propertyId", propertyId)
                    .getSingleResult();

            if (freeCancellationHours != null) {
                if (freeCancellationHours >= 48) {
                    return fullRefundPolicy.getDescription();
                }
                if (freeCancellationHours >= 24) {
                    return partialRefundPolicy.getDescription();
                }
                return noRefundPolicy.getDescription();
            }
        } catch (Exception e) {
            // Fall through to default
        }

        return fullRefundPolicy.getDescription();
    }
}
