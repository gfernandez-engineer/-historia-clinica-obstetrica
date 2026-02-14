package com.clinica.transcripcion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class TerminoMedico {

    private UUID id;
    private String termino;
    private String terminoNormalizado;
    private String codigoCie10;
    private String categoria;
}
