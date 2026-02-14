package com.clinica.historiaclinica.domain.port.in;

import com.clinica.historiaclinica.domain.model.Paciente;

import java.util.UUID;

public interface ObtenerPacienteUseCase {

    Paciente obtenerPorId(UUID id, UUID obstetraId);
}
