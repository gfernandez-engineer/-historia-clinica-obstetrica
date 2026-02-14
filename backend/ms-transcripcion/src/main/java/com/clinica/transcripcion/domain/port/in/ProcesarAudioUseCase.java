package com.clinica.transcripcion.domain.port.in;

import com.clinica.transcripcion.domain.model.Transcripcion;

import java.util.UUID;

public interface ProcesarAudioUseCase {

    Transcripcion procesar(ProcesarAudioCommand command);

    record ProcesarAudioCommand(
            UUID historiaClinicaId,
            UUID obstetraId,
            byte[] audioData,
            String contentType
    ) {
    }
}
