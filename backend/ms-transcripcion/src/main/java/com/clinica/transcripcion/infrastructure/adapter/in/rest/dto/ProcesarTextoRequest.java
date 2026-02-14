package com.clinica.transcripcion.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Solicitud para procesar texto de transcripción por voz")
public record ProcesarTextoRequest(

        @NotNull(message = "El ID de historia clínica es obligatorio")
        @Schema(description = "ID de la historia clínica asociada")
        UUID historiaClinicaId,

        @NotBlank(message = "El texto es obligatorio")
        @Size(min = 5, max = 10000, message = "El texto debe tener entre 5 y 10000 caracteres")
        @Schema(description = "Texto capturado por Web Speech API")
        String texto
) {}
