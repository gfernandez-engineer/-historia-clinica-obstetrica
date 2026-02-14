package com.clinica.historiaclinica.domain.port.in;

import com.clinica.historiaclinica.domain.model.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ActualizarHistoriaClinicaUseCase {

    HistoriaClinica actualizar(ActualizarHistoriaCommand command);

    record ActualizarHistoriaCommand(
            UUID id,
            UUID obstetraId,
            String notasGenerales,
            List<SeccionCommand> secciones,
            List<EventoCommand> eventos,
            List<MedicamentoCommand> medicamentos
    ) {
    }

    record SeccionCommand(
            TipoSeccion tipo,
            String contenido,
            OrigenContenido origen,
            int orden
    ) {
    }

    record EventoCommand(
            String tipo,
            Instant fecha,
            Integer semanaGestacional,
            String observaciones
    ) {
    }

    record MedicamentoCommand(
            String nombre,
            String dosis,
            String via,
            String frecuencia,
            String duracion
    ) {
    }
}
