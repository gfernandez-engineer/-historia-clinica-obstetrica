package com.clinica.historiaclinica.application.service;

import com.clinica.historiaclinica.domain.exception.PacienteNotFoundException;
import com.clinica.historiaclinica.domain.exception.PacienteYaExisteException;
import com.clinica.historiaclinica.domain.model.Paciente;
import com.clinica.historiaclinica.domain.port.in.ActualizarPacienteUseCase;
import com.clinica.historiaclinica.domain.port.in.CrearPacienteUseCase;
import com.clinica.historiaclinica.domain.port.in.ListarPacientesUseCase;
import com.clinica.historiaclinica.domain.port.in.ObtenerPacienteUseCase;
import com.clinica.historiaclinica.domain.port.out.HistoriaClinicaEventPublisherPort;
import com.clinica.historiaclinica.domain.port.out.PacienteRepositoryPort;
import com.clinica.shared.domain.event.AuditableEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PacienteService implements CrearPacienteUseCase, ObtenerPacienteUseCase,
        ListarPacientesUseCase, ActualizarPacienteUseCase {

    private final PacienteRepositoryPort pacienteRepository;
    private final HistoriaClinicaEventPublisherPort eventPublisher;

    @Override
    @Transactional
    public Paciente crear(CrearPacienteCommand command) {
        if (pacienteRepository.existsByDni(command.dni())) {
            throw new PacienteYaExisteException(command.dni());
        }

        Paciente paciente = Paciente.builder()
                .id(UUID.randomUUID())
                .dni(command.dni())
                .nombre(command.nombre())
                .apellido(command.apellido())
                .fechaNacimiento(command.fechaNacimiento())
                .telefono(command.telefono())
                .direccion(command.direccion())
                .obstetraId(command.obstetraId())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Paciente saved = pacienteRepository.save(paciente);

        eventPublisher.publish(AuditableEvent.create(
                "historia.paciente.creado",
                command.obstetraId(),
                null,
                "CREATE",
                "PACIENTE",
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
    public Paciente obtenerPorId(UUID id, UUID obstetraId) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new PacienteNotFoundException(id));

        if (!paciente.getObstetraId().equals(obstetraId)) {
            throw new PacienteNotFoundException(id);
        }

        return paciente;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Paciente> listarPorObstetra(UUID obstetraId, Pageable pageable) {
        return pacienteRepository.findByObstetraId(obstetraId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Paciente> buscarPorNombreODni(UUID obstetraId, String termino, Pageable pageable) {
        return pacienteRepository.searchByObstetraIdAndTerm(obstetraId, termino, pageable);
    }

    @Override
    @Transactional
    public Paciente actualizar(ActualizarPacienteCommand command) {
        Paciente existing = pacienteRepository.findById(command.id())
                .orElseThrow(() -> new PacienteNotFoundException(command.id()));

        if (!existing.getObstetraId().equals(command.obstetraId())) {
            throw new PacienteNotFoundException(command.id());
        }

        Paciente updated = Paciente.builder()
                .id(existing.getId())
                .dni(existing.getDni())
                .nombre(command.nombre())
                .apellido(command.apellido())
                .fechaNacimiento(command.fechaNacimiento())
                .telefono(command.telefono())
                .direccion(command.direccion())
                .obstetraId(existing.getObstetraId())
                .createdAt(existing.getCreatedAt())
                .updatedAt(Instant.now())
                .build();

        Paciente saved = pacienteRepository.save(updated);

        eventPublisher.publish(AuditableEvent.create(
                "historia.paciente.actualizado",
                command.obstetraId(),
                null,
                "UPDATE",
                "PACIENTE",
                saved.getId(),
                null,
                null,
                null,
                "ms-historia-clinica"
        ));

        return saved;
    }
}
