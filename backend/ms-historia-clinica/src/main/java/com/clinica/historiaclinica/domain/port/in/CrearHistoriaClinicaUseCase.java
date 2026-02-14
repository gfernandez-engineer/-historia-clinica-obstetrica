package com.clinica.historiaclinica.domain.port.in;

import com.clinica.historiaclinica.domain.model.HistoriaClinica;

import java.util.UUID;

public interface CrearHistoriaClinicaUseCase {

    HistoriaClinica crear(CrearHistoriaCommand command);

    record CrearHistoriaCommand(
            UUID pacienteId,
            UUID obstetraId,
            String notasGenerales
    ) {
    }
}
