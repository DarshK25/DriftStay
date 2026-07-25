package com.driftstay.booking.event;

import com.driftstay.booking.entity.Booking;
import com.driftstay.common.enums.BookingStatus;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Event published when a booking transitions to a new state.
 * Used for booking timeline population and cross-module communication.
 */
@Getter
public class BookingEvent {

    private final String eventType;
    private final Long bookingId;
    private final String bookingReference;
    private final BookingStatus previousStatus;
    private final BookingStatus newStatus;
    private final Long performedBy;
    private final String remarks;
    private final LocalDateTime timestamp;

    private BookingEvent(Builder builder) {
        this.eventType = builder.eventType;
        this.bookingId = builder.bookingId;
        this.bookingReference = builder.bookingReference;
        this.previousStatus = builder.previousStatus;
        this.newStatus = builder.newStatus;
        this.performedBy = builder.performedBy;
        this.remarks = builder.remarks;
        this.timestamp = builder.timestamp != null ? builder.timestamp : LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static BookingEvent fromBooking(Booking booking, String eventType,
                                           BookingStatus previousStatus, String performedBy,
                                           String remarks) {
        return BookingEvent.builder()
                .eventType(eventType)
                .bookingId(booking.getId())
                .bookingReference(booking.getBookingReference())
                .previousStatus(previousStatus)
                .newStatus(booking.getBookingStatus())
                .performedBy(performedBy != null ? Long.valueOf(performedBy) : null)
                .remarks(remarks)
                .build();
    }

    public static class Builder {
        private String eventType;
        private Long bookingId;
        private String bookingReference;
        private BookingStatus previousStatus;
        private BookingStatus newStatus;
        private Long performedBy;
        private String remarks;
        private LocalDateTime timestamp;

        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder bookingId(Long bookingId) {
            this.bookingId = bookingId;
            return this;
        }

        public Builder bookingReference(String bookingReference) {
            this.bookingReference = bookingReference;
            return this;
        }

        public Builder previousStatus(BookingStatus previousStatus) {
            this.previousStatus = previousStatus;
            return this;
        }

        public Builder newStatus(BookingStatus newStatus) {
            this.newStatus = newStatus;
            return this;
        }

        public Builder performedBy(Long performedBy) {
            this.performedBy = performedBy;
            return this;
        }

        public Builder remarks(String remarks) {
            this.remarks = remarks;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public BookingEvent build() {
            return new BookingEvent(this);
        }
    }
}
