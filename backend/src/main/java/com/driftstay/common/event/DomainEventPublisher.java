package com.driftstay.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publish(BaseDomainEvent event) {
        log.info("Publishing event: {} (id: {})", event.getEventType(), event.getEventId());
        publisher.publishEvent(event);
    }
}
