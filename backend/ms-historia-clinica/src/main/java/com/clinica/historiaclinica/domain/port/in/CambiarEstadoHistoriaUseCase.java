package com.clinica.historiaclinica.domain.port.in;

import com.clinica.historiaclinica.domain.model.HistoriaClinica;

import java.util.UUID;

public interface CambiarEstadoHistoriaUseCase {

    HistoriaClinica finalizar(UUID id, UUID obstetraId);

    HistoriaClinica pasarARevision(UUID id, UUID obstetraId);

    HistoriaClinica anular(UUID id, UUID obstetraId);
}
