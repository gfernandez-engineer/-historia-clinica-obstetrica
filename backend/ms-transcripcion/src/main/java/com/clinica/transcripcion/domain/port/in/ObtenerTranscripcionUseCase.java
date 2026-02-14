package com.clinica.transcripcion.domain.port.in;

import com.clinica.transcripcion.domain.model.Transcripcion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ObtenerTranscripcionUseCase {

    Transcripcion obtenerPorId(UUID id, UUID obstetraId);

    Page<Transcripcion> listarPorHistoria(UUID historiaClinicaId, UUID obstetraId, Pageable pageable);
}
