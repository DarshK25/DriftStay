package com.driftstay.booking.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventHandler {

    @EventListener
    public void handleBookingCreated(BookingCreatedEvent event) {
        log.info("Booking created event handled: {} - userId: {}, propertyId: {}",
                event.getBookingPublicId(), event.getUserId(), event.getPropertyId());
    }
}
