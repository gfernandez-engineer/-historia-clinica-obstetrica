package com.clinica.historiaclinica.domain.exception;

import com.clinica.shared.exception.DomainException;

public class PacienteYaExisteException extends DomainException {

    public PacienteYaExisteException(String dni) {
        super("Ya existe un paciente con DNI: " + dni);
    }
}
