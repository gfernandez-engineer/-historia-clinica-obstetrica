package com.clinica.historiaclinica.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Datos de paciente")
public record PacienteResponse(
        UUID id,
        String dni,
        String nombre,
        String apellido,
        LocalDate fechaNacimiento,
        String telefono,
        String direccion,
        Instant createdAt,
        Instant updatedAt
) {
}
