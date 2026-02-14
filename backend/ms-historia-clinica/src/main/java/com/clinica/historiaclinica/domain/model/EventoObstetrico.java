package com.clinica.historiaclinica.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class EventoObstetrico {

    private UUID id;
    private UUID historiaClinicaId;
    private String tipo;
    private Instant fecha;
    private Integer semanaGestacional;
    private String observaciones;
    private Instant createdAt;
}
