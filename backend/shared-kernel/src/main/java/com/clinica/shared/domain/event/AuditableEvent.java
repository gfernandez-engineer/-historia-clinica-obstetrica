package com.clinica.shared.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditableEvent implements DomainEvent {

    private UUID eventId;
    private Instant occurredOn;
    private String eventType;

    private UUID userId;
    private String userEmail;
    private String action;
    private String resourceType;
    private UUID resourceId;
    private String previousValue;
    private String newValue;
    private String sourceIp;
    private String sourceService;

    public static AuditableEvent create(String eventType, UUID userId, String userEmail,
                                         String action, String resourceType, UUID resourceId,
                                         String previousValue, String newValue,
                                         String sourceIp, String sourceService) {
        return AuditableEvent.builder()
                .eventId(UUID.randomUUID())
                .occurredOn(Instant.now())
                .eventType(eventType)
                .userId(userId)
                .userEmail(userEmail)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .previousValue(previousValue)
                .newValue(newValue)
                .sourceIp(sourceIp)
                .sourceService(sourceService)
                .build();
    }
}
