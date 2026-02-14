package com.clinica.historiaclinica.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Solicitud de creacion de historia clinica")
public record CrearHistoriaRequest(

        @Schema(description = "ID del paciente", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "El ID del paciente es obligatorio")
        UUID pacienteId,

        @Schema(description = "Notas generales iniciales")
        String notasGenerales
) {
}
