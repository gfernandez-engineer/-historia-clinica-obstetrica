package com.clinica.historiaclinica.application.service;

import com.clinica.historiaclinica.domain.exception.HistoriaClinicaException;
import com.clinica.historiaclinica.domain.exception.HistoriaNotFoundException;
import com.clinica.historiaclinica.domain.exception.PacienteNotFoundException;
import com.clinica.historiaclinica.domain.model.*;
import com.clinica.historiaclinica.domain.port.in.CrearHistoriaClinicaUseCase;
import com.clinica.historiaclinica.domain.port.out.HistoriaClinicaEventPublisherPort;
import com.clinica.historiaclinica.domain.port.out.HistoriaClinicaRepositoryPort;
import com.clinica.historiaclinica.domain.port.out.PacienteRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoriaClinicaServiceTest {

    @Mock
    private HistoriaClinicaRepositoryPort historiaRepository;

    @Mock
    private PacienteRepositoryPort pacienteRepository;

    @Mock
    private HistoriaClinicaEventPublisherPort eventPublisher;

    @InjectMocks
    private HistoriaClinicaService historiaService;

    private final UUID obstetraId = UUID.randomUUID();
    private final UUID pacienteId = UUID.randomUUID();

    private Paciente crearPaciente() {
        return Paciente.builder()
                .id(pacienteId).dni("12345678").nombre("Ana").apellido("Lopez")
                .obstetraId(obstetraId).createdAt(Instant.now()).updatedAt(Instant.now())
                .build();
    }

    private HistoriaClinica crearHistoria(EstadoHistoria estado) {
        return HistoriaClinica.builder()
                .id(UUID.randomUUID())
                .pacienteId(pacienteId)
                .version(1)
                .estado(estado)
                .obstetraId(obstetraId)
                .notasGenerales("Notas")
                .secciones(new ArrayList<>(List.of(
                        SeccionClinica.builder()
                                .id(UUID.randomUUID())
                                .tipo(TipoSeccion.DATOS_INGRESO)
                                .contenido("Contenido")
                                .origen(OrigenContenido.MANUAL)
                                .orden(1)
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build()
                )))
                .eventos(new ArrayList<>())
                .medicamentos(new ArrayList<>())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .jpaVersion(0L)
                .build();
    }

    @Nested
    @DisplayName("crear()")
    class Crear {

        @Test
        @DisplayName("Debe crear historia clinica exitosamente")
        void debeCrearHistoria() {
            when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(crearPaciente()));
            when(historiaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            var command = new CrearHistoriaClinicaUseCase.CrearHistoriaCommand(
                    pacienteId, obstetraId, "Notas iniciales");

            HistoriaClinica result = historiaService.crear(command);

            assertNotNull(result.getId());
            assertEquals(EstadoHistoria.BORRADOR, result.getEstado());
            assertEquals(1, result.getVersion());
            verify(eventPublisher).publish(any());
        }

        @Test
        @DisplayName("Debe fallar si paciente no existe")
        void debeFallarSiPacienteNoExiste() {
            when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.empty());

            var command = new CrearHistoriaClinicaUseCase.CrearHistoriaCommand(
                    pacienteId, obstetraId, "Notas");

            assertThrows(PacienteNotFoundException.class, () -> historiaService.crear(command));
        }

        @Test
        @DisplayName("Debe fallar si paciente no pertenece al obstetra")
        void debeFallarSiPacienteDeOtroObstetra() {
            Paciente pacienteOtro = Paciente.builder()
                    .id(pacienteId).dni("12345678").nombre("Ana").apellido("Lopez")
                    .obstetraId(UUID.randomUUID()).createdAt(Instant.now()).updatedAt(Instant.now())
                    .build();
            when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(pacienteOtro));

            var command = new CrearHistoriaClinicaUseCase.CrearHistoriaCommand(
                    pacienteId, obstetraId, "Notas");

            assertThrows(PacienteNotFoundException.class, () -> historiaService.crear(command));
        }
    }

    @Nested
    @DisplayName("finalizar()")
    class Finalizar {

        @Test
        @DisplayName("Debe finalizar historia en borrador")
        void debeFinalizarHistoria() {
            HistoriaClinica historia = crearHistoria(EstadoHistoria.BORRADOR);
            when(historiaRepository.findById(historia.getId())).thenReturn(Optional.of(historia));
            when(historiaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            HistoriaClinica result = historiaService.finalizar(historia.getId(), obstetraId);

            assertEquals(EstadoHistoria.FINALIZADA, result.getEstado());
            verify(eventPublisher).publish(any());
        }

        @Test
        @DisplayName("Debe fallar si no es owner")
        void debeFallarSiNoEsOwner() {
            HistoriaClinica historia = crearHistoria(EstadoHistoria.BORRADOR);
            when(historiaRepository.findById(historia.getId())).thenReturn(Optional.of(historia));

            assertThrows(HistoriaClinicaException.class,
                    () -> historiaService.finalizar(historia.getId(), UUID.randomUUID()));
        }

        @Test
        @DisplayName("Debe fallar si historia no existe")
        void debeFallarSiNoExiste() {
            UUID id = UUID.randomUUID();
            when(historiaRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(HistoriaNotFoundException.class,
                    () -> historiaService.finalizar(id, obstetraId));
        }
    }

    @Nested
    @DisplayName("obtenerPorId()")
    class ObtenerPorId {

        @Test
        @DisplayName("Debe obtener historia por ID")
        void debeObtenerHistoria() {
            HistoriaClinica historia = crearHistoria(EstadoHistoria.BORRADOR);
            when(historiaRepository.findById(historia.getId())).thenReturn(Optional.of(historia));

            HistoriaClinica result = historiaService.obtenerPorId(historia.getId(), obstetraId);

            assertEquals(historia.getId(), result.getId());
        }

        @Test
        @DisplayName("Debe fallar si no es owner")
        void debeFallarSiNoEsOwner() {
            HistoriaClinica historia = crearHistoria(EstadoHistoria.BORRADOR);
            when(historiaRepository.findById(historia.getId())).thenReturn(Optional.of(historia));

            assertThrows(HistoriaClinicaException.class,
                    () -> historiaService.obtenerPorId(historia.getId(), UUID.randomUUID()));
        }
    }
}
