package com.clinica.transcripcion.domain.port.in;

import com.clinica.transcripcion.domain.model.Transcripcion;

import java.util.UUID;

public interface ProcesarTextoUseCase {

    Transcripcion procesar(ProcesarTextoCommand command);

    record ProcesarTextoCommand(
            UUID historiaClinicaId,
            UUID obstetraId,
            String texto
    ) {
    }
}
