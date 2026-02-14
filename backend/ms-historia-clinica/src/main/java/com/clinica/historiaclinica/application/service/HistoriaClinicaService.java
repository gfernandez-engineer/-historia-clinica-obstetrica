package com.clinica.historiaclinica.application.service;

import com.clinica.historiaclinica.domain.exception.HistoriaNotFoundException;
import com.clinica.historiaclinica.domain.exception.PacienteNotFoundException;
import com.clinica.historiaclinica.domain.model.*;
import com.clinica.historiaclinica.domain.port.in.*;
import com.clinica.historiaclinica.domain.port.out.HistoriaClinicaEventPublisherPort;
import com.clinica.historiaclinica.domain.port.out.HistoriaClinicaRepositoryPort;
import com.clinica.historiaclinica.domain.port.out.PacienteRepositoryPort;
import com.clinica.shared.domain.event.AuditableEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HistoriaClinicaService implements CrearHistoriaClinicaUseCase, ObtenerHistoriaClinicaUseCase,
        ActualizarHistoriaClinicaUseCase, CambiarEstadoHistoriaUseCase, ListarHistoriasClinicasUseCase {

    private final HistoriaClinicaRepositoryPort historiaRepository;
    private final PacienteRepositoryPort pacienteRepository;
    private final HistoriaClinicaEventPublisherPort eventPublisher;

    @Override
    @Transactional
    public HistoriaClinica crear(CrearHistoriaCommand command) {
        // Validar que el paciente existe y pertenece al obstetra
        Paciente paciente = pacienteRepository.findById(command.pacienteId())
                .orElseThrow(() -> new PacienteNotFoundException(command.pacienteId()));

        if (!paciente.getObstetraId().equals(command.obstetraId())) {
            throw new PacienteNotFoundException(command.pacienteId());
        }

        HistoriaClinica historia = HistoriaClinica.builder()
                .id(UUID.randomUUID())
                .pacienteId(command.pacienteId())
                .version(1)
                .estado(EstadoHistoria.BORRADOR)
                .obstetraId(command.obstetraId())
                .notasGenerales(command.notasGenerales())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .jpaVersion(0L)
                .build();

        HistoriaClinica saved = historiaRepository.save(historia);

        eventPublisher.publish(AuditableEvent.create(
                "historia.historia-clinica.creada",
                command.obstetraId(),
                null,
                "CREATE",
                "HISTORIA_CLINICA",
                saved.getId(),
                null,
                null,
                null,
                "ms-historia-clinica"
        ));

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public HistoriaClinica obtenerPorId(UUID id, UUID obstetraId) {
        HistoriaClinica historia = historiaRepository.findById(id)
                .orElseThrow(() -> new HistoriaNotFoundException(id));

        historia.validarOwnership(obstetraId);
        return historia;
    }

    @Override
    @Transactional
    public HistoriaClinica actualizar(ActualizarHistoriaCommand command) {
        HistoriaClinica historia = historiaRepository.findById(command.id())
                .orElseThrow(() -> new HistoriaNotFoundException(command.id()));

        historia.validarOwnership(command.obstetraId());
        historia.validarEditable();

        // Construir secciones
        List<SeccionClinica> secciones = command.secciones() != null
                ? command.secciones().stream()
                    .map(s -> SeccionClinica.builder()
                            .id(UUID.randomUUID())
                            .historiaClinicaId(historia.getId())
                            .tipo(s.tipo())
                            .contenido(s.contenido())
                            .origen(s.origen())
                            .orden(s.orden())
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build())
                    .toList()
                : historia.getSecciones();

        // Construir eventos
        List<EventoObstetrico> eventos = command.eventos() != null
                ? command.eventos().stream()
                    .map(e -> EventoObstetrico.builder()
                            .id(UUID.randomUUID())
                            .historiaClinicaId(historia.getId())
                            .tipo(e.tipo())
                            .fecha(e.fecha())
                            .semanaGestacional(e.semanaGestacional())
                            .observaciones(e.observaciones())
                            .createdAt(Instant.now())
                            .build())
                    .toList()
                : historia.getEventos();

        // Construir medicamentos
        List<Medicamento> medicamentos = command.medicamentos() != null
                ? command.medicamentos().stream()
                    .map(m -> Medicamento.builder()
                            .id(UUID.randomUUID())
                            .historiaClinicaId(historia.getId())
                            .nombre(m.nombre())
                            .dosis(m.dosis())
                            .via(m.via())
                            .frecuencia(m.frecuencia())
                            .duracion(m.duracion())
                            .createdAt(Instant.now())
                            .build())
                    .toList()
                : historia.getMedicamentos();

        HistoriaClinica updated = HistoriaClinica.builder()
                .id(historia.getId())
                .pacienteId(historia.getPacienteId())
                .version(historia.getVersion())
                .estado(historia.getEstado())
                .obstetraId(historia.getObstetraId())
                .notasGenerales(command.notasGenerales() != null ? command.notasGenerales() : historia.getNotasGenerales())
                .secciones(secciones)
                .eventos(eventos)
                .medicamentos(medicamentos)
                .createdAt(historia.getCreatedAt())
                .updatedAt(Instant.now())
                .jpaVersion(historia.getJpaVersion())
                .build();

        HistoriaClinica saved = historiaRepository.save(updated);

        eventPublisher.publish(AuditableEvent.create(
                "historia.historia-clinica.actualizada",
                command.obstetraId(),
                null,
                "UPDATE",
                "HISTORIA_CLINICA",
                saved.getId(),
                null,
                null,
                null,
                "ms-historia-clinica"
        ));

        return saved;
    }

    @Override
    @Transactional
    public HistoriaClinica finalizar(UUID id, UUID obstetraId) {
        HistoriaClinica historia = historiaRepository.findById(id)
                .orElseThrow(() -> new HistoriaNotFoundException(id));

        historia.validarOwnership(obstetraId);
        historia.finalizar();

        HistoriaClinica saved = historiaRepository.save(historia);

        eventPublisher.publish(AuditableEvent.create(
                "historia.historia-clinica.finalizada",
                obstetraId,
                null,
                "FINALIZAR",
                "HISTORIA_CLINICA",
                saved.getId(),
                null,
                null,
                null,
                "ms-historia-clinica"
        ));

        return saved;
    }

    @Override
    @Transactional
    public HistoriaClinica pasarARevision(UUID id, UUID obstetraId) {
        HistoriaClinica historia = historiaRepository.findById(id)
                .orElseThrow(() -> new HistoriaNotFoundException(id));

        historia.validarOwnership(obstetraId);
        historia.pasarARevision();

        HistoriaClinica saved = historiaRepository.save(historia);

        eventPublisher.publish(AuditableEvent.create(
                "historia.historia-clinica.en-revision",
                obstetraId,
                null,
                "REVISION",
                "HISTORIA_CLINICA",
                saved.getId(),
                null,
                null,
                null,
                "ms-historia-clinica"
        ));

        return saved;
    }

    @Override
    @Transactional
    public HistoriaClinica anular(UUID id, UUID obstetraId) {
        HistoriaClinica historia = historiaRepository.findById(id)
                .orElseThrow(() -> new HistoriaNotFoundException(id));

        historia.validarOwnership(obstetraId);
        historia.anular();

        HistoriaClinica saved = historiaRepository.save(historia);

        eventPublisher.publish(AuditableEvent.create(
                "historia.historia-clinica.anulada",
                obstetraId,
                null,
                "ANULAR",
                "HISTORIA_CLINICA",
                saved.getId(),
                null,
                null,
                null,
                "ms-historia-clinica"
        ));

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistoriaClinica> listarPorObstetra(UUID obstetraId, Pageable pageable) {
        return historiaRepository.findByObstetraId(obstetraId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistoriaClinica> listarPorPaciente(UUID pacienteId, UUID obstetraId, Pageable pageable) {
        // Validar que el paciente pertenece al obstetra
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new PacienteNotFoundException(pacienteId));

        if (!paciente.getObstetraId().equals(obstetraId)) {
            throw new PacienteNotFoundException(pacienteId);
        }

        return historiaRepository.findByPacienteId(pacienteId, pageable);
    }
}
