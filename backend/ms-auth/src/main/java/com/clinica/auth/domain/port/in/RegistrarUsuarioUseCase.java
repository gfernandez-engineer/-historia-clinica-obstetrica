package com.clinica.auth.domain.port.in;

import com.clinica.auth.domain.model.Rol;
import com.clinica.auth.domain.model.Usuario;

public interface RegistrarUsuarioUseCase {

    Usuario registrar(RegistrarUsuarioCommand command);

    record RegistrarUsuarioCommand(
            String email,
            String password,
            String nombre,
            String apellido,
            Rol rol
    ) {
    }
}
