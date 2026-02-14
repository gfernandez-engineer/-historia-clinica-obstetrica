package com.clinica.historiaclinica.domain.exception;

import com.clinica.shared.exception.ResourceNotFoundException;

import java.util.UUID;

public class HistoriaNotFoundException extends ResourceNotFoundException {

    public HistoriaNotFoundException(UUID id) {
        super("Historia clinica", id);
    }
}
