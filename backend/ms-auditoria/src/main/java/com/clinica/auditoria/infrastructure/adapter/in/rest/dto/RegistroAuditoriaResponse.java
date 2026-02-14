package com.clinica.auditoria.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Registro de auditoria")
public record RegistroAuditoriaResponse(
        UUID id,
        UUID eventId,
        Instant occurredOn,
        String eventType,
        UUID userId,
        String userEmail,
        String action,
        String resourceType,
        UUID resourceId,
        String previousValue,
        String newValue,
        String sourceIp,
        String sourceService,
        Instant receivedAt
) {
}
