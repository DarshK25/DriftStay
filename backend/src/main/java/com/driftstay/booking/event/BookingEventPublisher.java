package com.driftstay.booking.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publishes booking lifecycle events to be consumed by:
 * - BookingTimelineService (populates timeline)
 * - NotificationService (sends notifications)
 * - AnalyticsService (tracks booking events)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Publish a booking event to the Spring application event bus.
     */
    public void publishBookingEvent(BookingEvent event) {
        log.debug("Publishing booking event: type={}, bookingRef={}, from={} to={}",
                event.getEventType(), event.getBookingReference(),
                event.getPreviousStatus(), event.getNewStatus());

        applicationEventPublisher.publishEvent(event);
    }
}
