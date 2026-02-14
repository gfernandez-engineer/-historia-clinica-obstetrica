package com.clinica.historiaclinica.domain.port.in;

import com.clinica.historiaclinica.domain.model.HistoriaClinica;

import java.util.UUID;

public interface ObtenerHistoriaClinicaUseCase {

    HistoriaClinica obtenerPorId(UUID id, UUID obstetraId);
}
