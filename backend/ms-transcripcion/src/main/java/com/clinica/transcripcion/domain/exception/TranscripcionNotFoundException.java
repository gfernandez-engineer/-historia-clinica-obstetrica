package com.clinica.transcripcion.domain.exception;

import com.clinica.shared.exception.ResourceNotFoundException;

import java.util.UUID;

public class TranscripcionNotFoundException extends ResourceNotFoundException {

    public TranscripcionNotFoundException(UUID id) {
        super("Transcripcion", id);
    }
}
