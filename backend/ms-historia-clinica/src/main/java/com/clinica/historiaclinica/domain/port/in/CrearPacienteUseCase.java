package com.clinica.historiaclinica.domain.port.in;

import com.clinica.historiaclinica.domain.model.Paciente;

import java.time.LocalDate;
import java.util.UUID;

public interface CrearPacienteUseCase {

    Paciente crear(CrearPacienteCommand command);

    record CrearPacienteCommand(
            String dni,
            String nombre,
            String apellido,
            LocalDate fechaNacimiento,
            String telefono,
            String direccion,
            UUID obstetraId
    ) {
    }
}
