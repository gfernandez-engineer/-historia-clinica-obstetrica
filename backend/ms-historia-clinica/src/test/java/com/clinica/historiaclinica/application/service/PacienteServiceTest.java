package com.clinica.historiaclinica.application.service;

import com.clinica.historiaclinica.domain.exception.PacienteNotFoundException;
import com.clinica.historiaclinica.domain.exception.PacienteYaExisteException;
import com.clinica.historiaclinica.domain.model.Paciente;
import com.clinica.historiaclinica.domain.port.in.CrearPacienteUseCase;
import com.clinica.historiaclinica.domain.port.out.HistoriaClinicaEventPublisherPort;
import com.clinica.historiaclinica.domain.port.out.PacienteRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteRepositoryPort pacienteRepository;

    @Mock
    private HistoriaClinicaEventPublisherPort eventPublisher;

    @InjectMocks
    private PacienteService pacienteService;

    private final UUID obstetraId = UUID.randomUUID();

    @Test
    @DisplayName("Debe crear paciente exitosamente")
    void debeCrearPaciente() {
        when(pacienteRepository.existsByDni("12345678")).thenReturn(false);
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

        var command = new CrearPacienteUseCase.CrearPacienteCommand(
                "12345678", "Ana", "Lopez", LocalDate.of(1990, 5, 15),
                "987654321", "Av. Lima 123", obstetraId);

        Paciente result = pacienteService.crear(command);

        assertNotNull(result.getId());
        assertEquals("12345678", result.getDni());
        assertEquals("Ana", result.getNombre());
        assertEquals(obstetraId, result.getObstetraId());
        verify(eventPublisher).publish(any());
    }

    @Test
    @DisplayName("Debe fallar si DNI ya existe")
    void debeFallarSiDniDuplicado() {
        when(pacienteRepository.existsByDni("12345678")).thenReturn(true);

        var command = new CrearPacienteUseCase.CrearPacienteCommand(
                "12345678", "Ana", "Lopez", LocalDate.of(1990, 5, 15),
                "987654321", "Av. Lima 123", obstetraId);

        assertThrows(PacienteYaExisteException.class, () -> pacienteService.crear(command));
        verify(pacienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe obtener paciente por ID si es del mismo obstetra")
    void debeObtenerPacientePorId() {
        UUID pacienteId = UUID.randomUUID();
        Paciente paciente = Paciente.builder()
                .id(pacienteId).dni("12345678").nombre("Ana").apellido("Lopez")
                .obstetraId(obstetraId).createdAt(Instant.now()).updatedAt(Instant.now())
                .build();

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));

        Paciente result = pacienteService.obtenerPorId(pacienteId, obstetraId);
        assertEquals(pacienteId, result.getId());
    }

    @Test
    @DisplayName("Debe fallar si paciente no pertenece al obstetra")
    void debeFallarSiNoEsDelObstetra() {
        UUID pacienteId = UUID.randomUUID();
        Paciente paciente = Paciente.builder()
                .id(pacienteId).dni("12345678").nombre("Ana").apellido("Lopez")
                .obstetraId(UUID.randomUUID()).createdAt(Instant.now()).updatedAt(Instant.now())
                .build();

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));

        assertThrows(PacienteNotFoundException.class,
                () -> pacienteService.obtenerPorId(pacienteId, obstetraId));
    }

    @Test
    @DisplayName("Debe fallar si paciente no existe")
    void debeFallarSiNoExiste() {
        UUID pacienteId = UUID.randomUUID();
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.empty());

        assertThrows(PacienteNotFoundException.class,
                () -> pacienteService.obtenerPorId(pacienteId, obstetraId));
    }
}
