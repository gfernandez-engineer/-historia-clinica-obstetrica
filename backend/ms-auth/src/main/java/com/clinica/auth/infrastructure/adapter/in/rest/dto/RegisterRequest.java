package com.clinica.auth.infrastructure.adapter.in.rest.dto;

import com.clinica.auth.domain.model.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Solicitud de registro de nuevo usuario")
public record RegisterRequest(

        @Schema(description = "Email del usuario", example = "maria.garcia@clinica.com")
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email no es valido")
        String email,

        @Schema(description = "Contrasena (minimo 8 caracteres)", example = "MiPassword123!")
        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
        String password,

        @Schema(description = "Nombre del usuario", example = "Maria")
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @Schema(description = "Apellido del usuario", example = "Garcia")
        @NotBlank(message = "El apellido es obligatorio")
        String apellido,

        @Schema(description = "Rol del usuario", example = "OBSTETRA")
        @NotNull(message = "El rol es obligatorio")
        Rol rol
) {
}
