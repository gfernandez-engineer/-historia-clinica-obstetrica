package com.clinica.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Usuario {

    private UUID id;
    private String email;
    private String passwordHash;
    private String nombre;
    private String apellido;
    private Rol rol;
    private boolean activo;
    private Instant createdAt;
    private Instant updatedAt;

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}
