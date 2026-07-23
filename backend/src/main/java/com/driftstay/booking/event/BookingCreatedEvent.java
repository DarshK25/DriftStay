package com.driftstay.booking.event;

import com.driftstay.common.event.BaseDomainEvent;
import lombok.Getter;

@Getter
public class BookingCreatedEvent extends BaseDomainEvent {

    private final String bookingPublicId;
    private final Long userId;
    private final Long propertyId;

    public BookingCreatedEvent(String bookingPublicId, Long userId, Long propertyId) {
        this.bookingPublicId = bookingPublicId;
        this.userId = userId;
        this.propertyId = propertyId;
    }

    @Override
    public String getEventType() {
        return "booking.created";
    }
}
