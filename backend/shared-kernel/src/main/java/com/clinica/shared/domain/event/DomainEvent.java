package com.clinica.shared.domain.event;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {

    UUID getEventId();

    Instant getOccurredOn();

    String getEventType();
}
