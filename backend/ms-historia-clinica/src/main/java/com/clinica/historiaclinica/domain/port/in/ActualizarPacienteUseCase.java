package com.clinica.historiaclinica.domain.port.in;

import com.clinica.historiaclinica.domain.model.Paciente;

import java.time.LocalDate;
import java.util.UUID;

public interface ActualizarPacienteUseCase {

    Paciente actualizar(ActualizarPacienteCommand command);

    record ActualizarPacienteCommand(
            UUID id,
            String nombre,
            String apellido,
            LocalDate fechaNacimiento,
            String telefono,
            String direccion,
            UUID obstetraId
    ) {
    }
}
