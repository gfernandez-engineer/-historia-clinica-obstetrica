package com.clinica.auth.domain.exception;

public class CredencialesInvalidasException extends AuthException {

    public CredencialesInvalidasException() {
        super("Email o contrasena incorrectos");
    }
}
