package com.clinica.historiaclinica.infrastructure.adapter.in.rest.dto;

import com.clinica.historiaclinica.domain.model.OrigenContenido;
import com.clinica.historiaclinica.domain.model.TipoSeccion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

import java.time.Instant;
import java.util.List;

@Schema(description = "Solicitud de actualizacion de historia clinica")
public record ActualizarHistoriaRequest(

        @Schema(description = "Notas generales")
        String notasGenerales,

        @Schema(description = "Secciones clinicas")
        @Valid
        List<SeccionRequest> secciones,

        @Schema(description = "Eventos obstetricos")
        @Valid
        List<EventoRequest> eventos,

        @Schema(description = "Medicamentos")
        @Valid
        List<MedicamentoRequest> medicamentos
) {

    @Schema(description = "Seccion clinica")
    public record SeccionRequest(
            TipoSeccion tipo,
            String contenido,
            OrigenContenido origen,
            int orden
    ) {
    }

    @Schema(description = "Evento obstetrico")
    public record EventoRequest(
            String tipo,
            Instant fecha,
            Integer semanaGestacional,
            String observaciones
    ) {
    }

    @Schema(description = "Medicamento")
    public record MedicamentoRequest(
            String nombre,
            String dosis,
            String via,
            String frecuencia,
            String duracion
    ) {
    }
}
