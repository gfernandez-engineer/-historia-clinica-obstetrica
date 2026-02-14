package com.clinica.historiaclinica.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Medicamento {

    private UUID id;
    private UUID historiaClinicaId;
    private String nombre;
    private String dosis;
    private String via;
    private String frecuencia;
    private String duracion;
    private Instant createdAt;
}
