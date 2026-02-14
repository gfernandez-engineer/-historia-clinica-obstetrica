package com.clinica.exportacion.infrastructure.adapter.in.rest.dto;

import com.clinica.exportacion.domain.model.EstadoExportacion;
import com.clinica.exportacion.domain.model.FormatoExportacion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Informacion de un job de exportacion")
public record ExportJobResponse(
        UUID id,
        UUID historiaClinicaId,
        FormatoExportacion formato,
        EstadoExportacion estado,
        String archivoUrl,
        String errorMensaje,
        Instant createdAt,
        Instant completedAt
) {
}
