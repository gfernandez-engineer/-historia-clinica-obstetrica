package com.clinica.auth.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Solicitud de inicio de sesion")
public record LoginRequest(

        @Schema(description = "Email del usuario", example = "maria.garcia@clinica.com")
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email no es valido")
        String email,

        @Schema(description = "Contrasena del usuario", example = "MiPassword123!")
        @NotBlank(message = "La contrasena es obligatoria")
        String password
) {
}
