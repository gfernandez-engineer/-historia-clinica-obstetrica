package com.clinica.transcripcion.infrastructure.adapter.in.rest.dto;

import com.clinica.transcripcion.domain.model.EstadoTranscripcion;
import com.clinica.transcripcion.domain.model.OrigenTranscripcion;
import com.clinica.transcripcion.domain.model.Transcripcion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Respuesta con datos de una transcripción")
public record TranscripcionResponse(
        UUID id,
        UUID historiaClinicaId,
        UUID obstetraId,
        String textoOriginal,
        String textoNormalizado,
        EstadoTranscripcion estado,
        OrigenTranscripcion origen,
        String errorDetalle,
        Instant createdAt,
        Instant updatedAt
) {
    public static TranscripcionResponse fromDomain(Transcripcion t) {
        return new TranscripcionResponse(
                t.getId(),
                t.getHistoriaClinicaId(),
                t.getObstetraId(),
                t.getTextoOriginal(),
                t.getTextoNormalizado(),
                t.getEstado(),
                t.getOrigen(),
                t.getErrorDetalle(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}
