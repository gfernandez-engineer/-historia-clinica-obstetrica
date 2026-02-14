package com.clinica.exportacion.domain.exception;

import com.clinica.shared.exception.DomainException;

public class ExportacionException extends DomainException {

    public ExportacionException(String message) {
        super(message);
    }

    public ExportacionException(String message, Throwable cause) {
        super(message, cause);
    }
}
