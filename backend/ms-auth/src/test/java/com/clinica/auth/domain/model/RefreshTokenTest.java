package com.clinica.auth.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenTest {

    @Test
    @DisplayName("Token valido: no expirado y no revocado")
    void tokenValidoDebeRetornarTrue() {
        RefreshToken token = RefreshToken.builder()
                .id(UUID.randomUUID())
                .usuarioId(UUID.randomUUID())
                .token("some-token")
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .revoked(false)
                .createdAt(Instant.now())
                .build();

        assertThat(token.isValid()).isTrue();
        assertThat(token.isExpired()).isFalse();
    }

    @Test
    @DisplayName("Token expirado debe ser invalido")
    void tokenExpiradoDebeSerInvalido() {
        RefreshToken token = RefreshToken.builder()
                .id(UUID.randomUUID())
                .usuarioId(UUID.randomUUID())
                .token("some-token")
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .revoked(false)
                .createdAt(Instant.now().minus(8, ChronoUnit.DAYS))
                .build();

        assertThat(token.isValid()).isFalse();
        assertThat(token.isExpired()).isTrue();
    }

    @Test
    @DisplayName("Token revocado debe ser invalido")
    void tokenRevocadoDebeSerInvalido() {
        RefreshToken token = RefreshToken.builder()
                .id(UUID.randomUUID())
                .usuarioId(UUID.randomUUID())
                .token("some-token")
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .revoked(true)
                .createdAt(Instant.now())
                .build();

        assertThat(token.isValid()).isFalse();
        assertThat(token.isExpired()).isFalse();
    }
}
