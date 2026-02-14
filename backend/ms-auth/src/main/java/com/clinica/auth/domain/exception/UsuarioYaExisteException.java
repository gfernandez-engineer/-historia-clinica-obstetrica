package com.clinica.auth.domain.exception;

public class UsuarioYaExisteException extends AuthException {

    public UsuarioYaExisteException(String email) {
        super("Ya existe un usuario registrado con el email: " + email);
    }
}
