package com.clinica.auth.domain.exception;

import com.clinica.shared.exception.DomainException;

public class AuthException extends DomainException {

    public AuthException(String message) {
        super(message);
    }
}
