package com.clinica.historiaclinica.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Paciente {

    private UUID id;
    private String dni;
    private String nombre;
    private String apellido;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String direccion;
    private UUID obstetraId;
    private Instant createdAt;
    private Instant updatedAt;

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}
