package com.clinica.auth.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con token de acceso")
public record TokenResponse(

        @Schema(description = "Token JWT de acceso (15 min)")
        String accessToken,

        @Schema(description = "Tipo de token", example = "Bearer")
        String tokenType
) {
    public TokenResponse(String accessToken) {
        this(accessToken, "Bearer");
    }
}
