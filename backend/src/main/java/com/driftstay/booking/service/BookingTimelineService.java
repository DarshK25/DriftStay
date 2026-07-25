package com.driftstay.booking.service;

import com.driftstay.booking.entity.Booking;
import com.driftstay.booking.entity.BookingTimeline;
import com.driftstay.booking.event.BookingEvent;
import com.driftstay.booking.repository.BookingRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Automatically populates the booking timeline whenever a BookingEvent is published.
 * This is the sole mechanism for timeline growth - events are the single source of truth.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingTimelineService {

    private final BookingRepository bookingRepository;
    private final EntityManager entityManager;

    /**
     * Listen for booking events and create a timeline entry.
     * Uses AFTER_COMMIT to ensure the timeline entry is only created
     * after the booking transaction has successfully committed.
     * This prevents orphaned timeline entries if the booking rollbacks.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleBookingEvent(BookingEvent event) {
        Booking booking = bookingRepository.findById(event.getBookingId())
                .orElse(null);

        if (booking == null) {
            log.warn("Cannot create timeline entry: Booking {} not found", event.getBookingId());
            return;
        }

        String performedBy = event.getPerformedBy() != null
                ? "USER:" + event.getPerformedBy()
                : "SYSTEM";

        String remarks = buildRemarks(event);

        BookingTimeline timelineEntry = new BookingTimeline();
        timelineEntry.setBooking(booking);
        timelineEntry.setEventType(event.getEventType());
        timelineEntry.setPerformedBy(performedBy);
        timelineEntry.setRemarks(remarks);

        entityManager.persist(timelineEntry);

        log.debug("Timeline entry created for booking {}: event={}",
                booking.getBookingReference(), event.getEventType());
    }

    private String buildRemarks(BookingEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("Status changed from ").append(event.getPreviousStatus())
                .append(" to ").append(event.getNewStatus());

        if (event.getRemarks() != null && !event.getRemarks().isBlank()) {
            sb.append(". ").append(event.getRemarks());
        }

        return sb.toString();
    }
}
