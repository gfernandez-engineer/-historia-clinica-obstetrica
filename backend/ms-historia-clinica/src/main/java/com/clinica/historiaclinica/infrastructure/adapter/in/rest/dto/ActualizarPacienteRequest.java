package com.clinica.historiaclinica.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

@Schema(description = "Solicitud de actualizacion de paciente")
public record ActualizarPacienteRequest(

        @Schema(description = "Nombre del paciente", example = "Ana")
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @Schema(description = "Apellido del paciente", example = "Lopez")
        @NotBlank(message = "El apellido es obligatorio")
        String apellido,

        @Schema(description = "Fecha de nacimiento", example = "1990-05-15")
        @Past(message = "La fecha de nacimiento debe ser en el pasado")
        LocalDate fechaNacimiento,

        @Schema(description = "Telefono de contacto", example = "987654321")
        String telefono,

        @Schema(description = "Direccion del paciente", example = "Av. Lima 123")
        String direccion
) {
}
