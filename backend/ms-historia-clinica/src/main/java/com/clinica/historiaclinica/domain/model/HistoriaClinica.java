package com.clinica.historiaclinica.domain.model;

import com.clinica.historiaclinica.domain.exception.HistoriaClinicaException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class HistoriaClinica {

    private UUID id;
    private UUID pacienteId;
    private int version;
    private EstadoHistoria estado;
    private UUID obstetraId;
    private String notasGenerales;
    private Instant createdAt;
    private Instant updatedAt;
    private Long jpaVersion;

    @Builder.Default
    private List<SeccionClinica> secciones = new ArrayList<>();

    @Builder.Default
    private List<EventoObstetrico> eventos = new ArrayList<>();

    @Builder.Default
    private List<Medicamento> medicamentos = new ArrayList<>();

    public boolean isEditable() {
        return estado == EstadoHistoria.BORRADOR || estado == EstadoHistoria.EN_REVISION;
    }

    public void validarEditable() {
        if (!isEditable()) {
            throw new HistoriaClinicaException(
                    "No se puede modificar una historia clinica en estado " + estado);
        }
    }

    public void finalizar() {
        if (estado == EstadoHistoria.FINALIZADA) {
            throw new HistoriaClinicaException("La historia clinica ya esta finalizada");
        }
        if (estado == EstadoHistoria.ANULADA) {
            throw new HistoriaClinicaException("No se puede finalizar una historia clinica anulada");
        }
        if (secciones.isEmpty()) {
            throw new HistoriaClinicaException("No se puede finalizar una historia clinica sin secciones");
        }
        this.estado = EstadoHistoria.FINALIZADA;
        this.updatedAt = Instant.now();
    }

    public void anular() {
        if (estado == EstadoHistoria.ANULADA) {
            throw new HistoriaClinicaException("La historia clinica ya esta anulada");
        }
        this.estado = EstadoHistoria.ANULADA;
        this.updatedAt = Instant.now();
    }

    public void pasarARevision() {
        if (estado != EstadoHistoria.BORRADOR) {
            throw new HistoriaClinicaException("Solo se puede pasar a revision desde estado BORRADOR");
        }
        this.estado = EstadoHistoria.EN_REVISION;
        this.updatedAt = Instant.now();
    }

    /**
     * Crea una nueva version de esta historia clinica para edicion post-finalizacion.
     * La historia original permanece inmutable.
     */
    public HistoriaClinica crearNuevaVersion() {
        if (estado != EstadoHistoria.FINALIZADA) {
            throw new HistoriaClinicaException(
                    "Solo se puede crear nueva version de una historia finalizada");
        }

        return HistoriaClinica.builder()
                .id(UUID.randomUUID())
                .pacienteId(this.pacienteId)
                .version(this.version + 1)
                .estado(EstadoHistoria.BORRADOR)
                .obstetraId(this.obstetraId)
                .notasGenerales(this.notasGenerales)
                .secciones(new ArrayList<>(this.secciones))
                .eventos(new ArrayList<>(this.eventos))
                .medicamentos(new ArrayList<>(this.medicamentos))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .jpaVersion(0L)
                .build();
    }

    public void validarOwnership(UUID requestObstetraId) {
        if (!this.obstetraId.equals(requestObstetraId)) {
            throw new HistoriaClinicaException("No tiene permiso para acceder a esta historia clinica");
        }
    }
}
