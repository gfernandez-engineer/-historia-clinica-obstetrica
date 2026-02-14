package com.clinica.exportacion.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ExportJob {

    private UUID id;
    private UUID historiaClinicaId;
    private UUID obstetraId;
    private FormatoExportacion formato;
    private EstadoExportacion estado;
    private String archivoUrl;
    private String errorMensaje;
    private Instant createdAt;
    private Instant completedAt;

    public static ExportJob crear(UUID historiaClinicaId, UUID obstetraId, FormatoExportacion formato) {
        return ExportJob.builder()
                .id(UUID.randomUUID())
                .historiaClinicaId(historiaClinicaId)
                .obstetraId(obstetraId)
                .formato(formato)
                .estado(EstadoExportacion.PENDIENTE)
                .createdAt(Instant.now())
                .build();
    }

    public void marcarProcesando() {
        this.estado = EstadoExportacion.PROCESANDO;
    }

    public void marcarCompletado(String archivoUrl) {
        this.estado = EstadoExportacion.COMPLETADO;
        this.archivoUrl = archivoUrl;
        this.completedAt = Instant.now();
    }

    public void marcarError(String mensaje) {
        this.estado = EstadoExportacion.ERROR;
        this.errorMensaje = mensaje;
        this.completedAt = Instant.now();
    }
}
