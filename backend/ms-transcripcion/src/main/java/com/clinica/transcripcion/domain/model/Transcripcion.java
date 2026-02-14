package com.clinica.transcripcion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Transcripcion {

    private UUID id;
    private UUID historiaClinicaId;
    private UUID obstetraId;
    private String textoOriginal;
    private String textoNormalizado;
    private EstadoTranscripcion estado;
    private OrigenTranscripcion origen;
    private String errorDetalle;
    private Instant createdAt;
    private Instant updatedAt;

    public void completar(String textoNormalizado) {
        this.textoNormalizado = textoNormalizado;
        this.estado = EstadoTranscripcion.COMPLETADA;
        this.updatedAt = Instant.now();
    }

    public void marcarError(String detalle) {
        this.estado = EstadoTranscripcion.ERROR;
        this.errorDetalle = detalle;
        this.updatedAt = Instant.now();
    }
}
