package com.clinica.exportacion.infrastructure.adapter.in.rest.dto;

import com.clinica.exportacion.domain.model.FormatoExportacion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Solicitud de exportacion de historia clinica")
public record GenerarExportacionRequest(

        @NotNull(message = "El ID de historia clinica es obligatorio")
        @Schema(description = "ID de la historia clinica a exportar")
        UUID historiaClinicaId,

        @Schema(description = "Formato de exportacion", defaultValue = "PDF")
        FormatoExportacion formato
) {
    public GenerarExportacionRequest {
        if (formato == null) {
            formato = FormatoExportacion.PDF;
        }
    }
}
