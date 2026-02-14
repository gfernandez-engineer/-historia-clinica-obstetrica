package com.clinica.exportacion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class DatosHistoriaClinica {

    private UUID id;
    private int version;
    private String estado;
    private String notasGenerales;
    private Instant createdAt;

    private String pacienteNombre;
    private String pacienteApellido;
    private String pacienteDni;
    private LocalDate pacienteFechaNacimiento;
    private String pacienteTelefono;
    private String pacienteDireccion;

    private List<Seccion> secciones;
    private List<Evento> eventos;
    private List<Medicamento> medicamentos;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Seccion {
        private String tipo;
        private String contenido;
        private int orden;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Evento {
        private String tipo;
        private Instant fecha;
        private Integer semanaGestacional;
        private String observaciones;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Medicamento {
        private String nombre;
        private String dosis;
        private String via;
        private String frecuencia;
        private String duracion;
    }
}
