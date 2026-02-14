package com.clinica.historiaclinica.domain.exception;

import com.clinica.shared.exception.ResourceNotFoundException;

import java.util.UUID;

public class PacienteNotFoundException extends ResourceNotFoundException {

    public PacienteNotFoundException(UUID id) {
        super("Paciente", id);
    }

    public PacienteNotFoundException(String dni) {
        super("Paciente", "dni", dni);
    }
}
