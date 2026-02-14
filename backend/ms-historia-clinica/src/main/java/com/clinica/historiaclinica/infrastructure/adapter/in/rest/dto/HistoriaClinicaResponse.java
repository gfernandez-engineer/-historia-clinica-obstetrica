package com.clinica.historiaclinica.infrastructure.adapter.in.rest.dto;

import com.clinica.historiaclinica.domain.model.EstadoHistoria;
import com.clinica.historiaclinica.domain.model.OrigenContenido;
import com.clinica.historiaclinica.domain.model.TipoSeccion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Datos de historia clinica")
public record HistoriaClinicaResponse(
        UUID id,
        UUID pacienteId,
        int version,
        EstadoHistoria estado,
        String notasGenerales,
        List<SeccionResponse> secciones,
        List<EventoResponse> eventos,
        List<MedicamentoResponse> medicamentos,
        Instant createdAt,
        Instant updatedAt
) {

    public record SeccionResponse(
            UUID id,
            TipoSeccion tipo,
            String contenido,
            OrigenContenido origen,
            int orden
    ) {
    }

    public record EventoResponse(
            UUID id,
            String tipo,
            Instant fecha,
            Integer semanaGestacional,
            String observaciones
    ) {
    }

    public record MedicamentoResponse(
            UUID id,
            String nombre,
            String dosis,
            String via,
            String frecuencia,
            String duracion
    ) {
    }
}
