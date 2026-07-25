package com.driftstay.booking.service;

import com.driftstay.booking.entity.Booking;
import com.driftstay.booking.entity.BookingTimeline;
import com.driftstay.booking.event.BookingEvent;
import com.driftstay.booking.event.BookingEventPublisher;
import com.driftstay.booking.repository.BookingRepository;
import com.driftstay.common.enums.BookingStatus;
import com.driftstay.exception.InvalidBookingStateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages the booking lifecycle state machine.
 *
 * Valid state transitions:
 * PENDING     → CONFIRMED, CANCELLED, FAILED
 * CONFIRMED   → CHECKED_IN, CANCELLED, REFUNDED
 * CHECKED_IN  → CHECKED_OUT
 * CHECKED_OUT → COMPLETED
 * CANCELLED   → REFUNDED (terminal)
 * FAILED      → PENDING (retry)
 * COMPLETED   → (terminal)
 * REFUNDED    → (terminal)
 * NO_SHOW     → (terminal)
 *
 * Illegal transitions throw InvalidBookingStateException.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingLifecycleService {

    private final BookingRepository bookingRepository;
    private final BookingEventPublisher eventPublisher;

    private static final Map<BookingStatus, Set<BookingStatus>> VALID_TRANSITIONS = new EnumMap<>(BookingStatus.class);

    static {
        VALID_TRANSITIONS.put(BookingStatus.PENDING,
                EnumSet.of(BookingStatus.CONFIRMED, BookingStatus.CANCELLED, BookingStatus.FAILED));
        VALID_TRANSITIONS.put(BookingStatus.CONFIRMED,
                EnumSet.of(BookingStatus.CHECKED_IN, BookingStatus.CANCELLED, BookingStatus.REFUNDED));
        VALID_TRANSITIONS.put(BookingStatus.CHECKED_IN,
                EnumSet.of(BookingStatus.CHECKED_OUT));
        VALID_TRANSITIONS.put(BookingStatus.CHECKED_OUT,
                EnumSet.of(BookingStatus.COMPLETED));
        VALID_TRANSITIONS.put(BookingStatus.CANCELLED,
                EnumSet.of(BookingStatus.REFUNDED));
        VALID_TRANSITIONS.put(BookingStatus.FAILED,
                EnumSet.of(BookingStatus.PENDING));
        // COMPLETED, REFUNDED, NO_SHOW are terminal states (no outgoing transitions)
        VALID_TRANSITIONS.put(BookingStatus.COMPLETED, EnumSet.noneOf(BookingStatus.class));
        VALID_TRANSITIONS.put(BookingStatus.REFUNDED, EnumSet.noneOf(BookingStatus.class));
        VALID_TRANSITIONS.put(BookingStatus.NO_SHOW, EnumSet.noneOf(BookingStatus.class));
    }

    // Event type constants for booking timeline
    public static final String EVENT_CREATED = "BOOKING_CREATED";
    public static final String EVENT_CONFIRMED = "BOOKING_CONFIRMED";
    public static final String EVENT_CHECKED_IN = "CHECKED_IN";
    public static final String EVENT_CHECKED_OUT = "CHECKED_OUT";
    public static final String EVENT_COMPLETED = "BOOKING_COMPLETED";
    public static final String EVENT_CANCELLED = "BOOKING_CANCELLED";
    public static final String EVENT_FAILED = "BOOKING_FAILED";
    public static final String EVENT_REFUNDED = "BOOKING_REFUNDED";
    public static final String EVENT_PAYMENT_RECEIVED = "PAYMENT_RECEIVED";
    public static final String EVENT_MODIFIED = "BOOKING_MODIFIED";

    /**
     * Transition a booking to a new status with validation.
     *
     * @param bookingId      The booking to transition
     * @param targetStatus   The target status
     * @param performedBy    Who performed the action (userId as string)
     * @param remarks        Optional remarks
     * @return The updated booking
     * @throws InvalidBookingStateException if the transition is not allowed
     */
    @Transactional
    public Booking transitionStatus(Long bookingId, BookingStatus targetStatus,
                                    String performedBy, String remarks) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        BookingStatus currentStatus = booking.getBookingStatus();

        // Validate the transition
        validateTransition(currentStatus, targetStatus, booking.getBookingReference());

        // Perform the transition
        BookingStatus previousStatus = booking.getBookingStatus();
        booking.setBookingStatus(targetStatus);

        if (targetStatus == BookingStatus.CONFIRMED || targetStatus == BookingStatus.CHECKED_IN) {
            booking.setPaymentStatus(com.driftstay.common.enums.PaymentStatus.PAID);
        }

        booking = bookingRepository.save(booking);

        // Determine event type
        String eventType = getEventType(targetStatus);

        // Publish event for timeline and other subscribers
        BookingEvent event = BookingEvent.builder()
                .eventType(eventType)
                .bookingId(booking.getId())
                .bookingReference(booking.getBookingReference())
                .previousStatus(previousStatus)
                .newStatus(targetStatus)
                .performedBy(performedBy != null ? Long.valueOf(performedBy) : null)
                .remarks(remarks)
                .timestamp(LocalDateTime.now())
                .build();

        eventPublisher.publishBookingEvent(event);

        log.info("Booking {} transitioned: {} -> {} (by {})",
                booking.getBookingReference(), previousStatus, targetStatus, performedBy);

        return booking;
    }

    /**
     * Validate a booking state transition.
     * Public so other services (BookingService) can check transitions without executing them.
     */
    public void validateTransition(BookingStatus current, BookingStatus target, String bookingReference) {
        Set<BookingStatus> allowedTransitions = VALID_TRANSITIONS.get(current);

        if (allowedTransitions == null || !allowedTransitions.contains(target)) {
            throw new InvalidBookingStateException(current, target, bookingReference);
        }
    }

    /**
     * Map a BookingStatus to an event type string.
     */
    private String getEventType(BookingStatus status) {
        return switch (status) {
            case CONFIRMED -> EVENT_CONFIRMED;
            case CHECKED_IN -> EVENT_CHECKED_IN;
            case CHECKED_OUT -> EVENT_CHECKED_OUT;
            case COMPLETED -> EVENT_COMPLETED;
            case CANCELLED -> EVENT_CANCELLED;
            case FAILED -> EVENT_FAILED;
            case REFUNDED -> EVENT_REFUNDED;
            default -> "BOOKING_" + status.name();
        };
    }
}
