package com.clinica.auth.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Solicitud de renovacion de token")
public record RefreshRequest(

        @Schema(description = "Refresh token obtenido en login")
        @NotBlank(message = "El refresh token es obligatorio")
        String refreshToken
) {
}
