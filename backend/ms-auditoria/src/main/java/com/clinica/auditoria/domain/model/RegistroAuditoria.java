package com.clinica.auditoria.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class RegistroAuditoria {

    private UUID id;
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
    private Instant receivedAt;
}
