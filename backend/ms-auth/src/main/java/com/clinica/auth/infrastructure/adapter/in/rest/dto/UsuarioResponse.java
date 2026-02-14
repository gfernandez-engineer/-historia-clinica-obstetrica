package com.clinica.auth.infrastructure.adapter.in.rest.dto;

import com.clinica.auth.domain.model.Rol;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Datos del usuario registrado")
public record UsuarioResponse(

        @Schema(description = "ID del usuario")
        UUID id,

        @Schema(description = "Email", example = "maria.garcia@clinica.com")
        String email,

        @Schema(description = "Nombre", example = "Maria")
        String nombre,

        @Schema(description = "Apellido", example = "Garcia")
        String apellido,

        @Schema(description = "Rol asignado", example = "OBSTETRA")
        Rol rol,

        @Schema(description = "Fecha de creacion")
        Instant createdAt
) {
}
