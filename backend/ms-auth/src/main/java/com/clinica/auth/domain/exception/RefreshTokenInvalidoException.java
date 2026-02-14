package com.clinica.auth.domain.exception;

public class RefreshTokenInvalidoException extends AuthException {

    public RefreshTokenInvalidoException() {
        super("Refresh token invalido, expirado o revocado");
    }
}
