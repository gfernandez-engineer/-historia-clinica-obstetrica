package com.clinica.historiaclinica.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class SeccionClinica {

    private UUID id;
    private UUID historiaClinicaId;
    private TipoSeccion tipo;
    private String contenido;
    private OrigenContenido origen;
    private int orden;
    private Instant createdAt;
    private Instant updatedAt;
}
