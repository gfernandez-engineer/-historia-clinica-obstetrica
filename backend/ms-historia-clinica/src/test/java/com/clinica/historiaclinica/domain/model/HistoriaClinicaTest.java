package com.clinica.historiaclinica.domain.model;

import com.clinica.historiaclinica.domain.exception.HistoriaClinicaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HistoriaClinicaTest {

    private HistoriaClinica crearHistoriaBorrador() {
        return HistoriaClinica.builder()
                .id(UUID.randomUUID())
                .pacienteId(UUID.randomUUID())
                .version(1)
                .estado(EstadoHistoria.BORRADOR)
                .obstetraId(UUID.randomUUID())
                .notasGenerales("Notas de prueba")
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
    @DisplayName("isEditable()")
    class IsEditable {

        @Test
        void debeSerEditableEnBorrador() {
            HistoriaClinica historia = crearHistoriaBorrador();
            assertTrue(historia.isEditable());
        }

        @Test
        void debeSerEditableEnRevision() {
            HistoriaClinica historia = crearHistoriaBorrador();
            historia.pasarARevision();
            assertTrue(historia.isEditable());
        }

        @Test
        void noDebeSerEditableSiFinalizada() {
            HistoriaClinica historia = crearHistoriaBorrador();
            historia.finalizar();
            assertFalse(historia.isEditable());
        }

        @Test
        void noDebeSerEditableSiAnulada() {
            HistoriaClinica historia = crearHistoriaBorrador();
            historia.anular();
            assertFalse(historia.isEditable());
        }
    }

    @Nested
    @DisplayName("finalizar()")
    class Finalizar {

        @Test
        void debeFinalizarDesdeBorrador() {
            HistoriaClinica historia = crearHistoriaBorrador();
            historia.finalizar();
            assertEquals(EstadoHistoria.FINALIZADA, historia.getEstado());
        }

        @Test
        void debeFinalizarDesdeRevision() {
            HistoriaClinica historia = crearHistoriaBorrador();
            historia.pasarARevision();
            historia.finalizar();
            assertEquals(EstadoHistoria.FINALIZADA, historia.getEstado());
        }

        @Test
        void debeFallarSiYaFinalizada() {
            HistoriaClinica historia = crearHistoriaBorrador();
            historia.finalizar();
            assertThrows(HistoriaClinicaException.class, historia::finalizar);
        }

        @Test
        void debeFallarSiAnulada() {
            HistoriaClinica historia = crearHistoriaBorrador();
            historia.anular();
            assertThrows(HistoriaClinicaException.class, historia::finalizar);
        }

        @Test
        void debeFallarSinSecciones() {
            HistoriaClinica historia = HistoriaClinica.builder()
                    .id(UUID.randomUUID())
                    .pacienteId(UUID.randomUUID())
                    .version(1)
                    .estado(EstadoHistoria.BORRADOR)
                    .obstetraId(UUID.randomUUID())
                    .secciones(new ArrayList<>())
                    .eventos(new ArrayList<>())
                    .medicamentos(new ArrayList<>())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .jpaVersion(0L)
                    .build();

            assertThrows(HistoriaClinicaException.class, historia::finalizar);
        }
    }

    @Nested
    @DisplayName("pasarARevision()")
    class PasarARevision {

        @Test
        void debePasarARevisionDesdeBorrador() {
            HistoriaClinica historia = crearHistoriaBorrador();
            historia.pasarARevision();
            assertEquals(EstadoHistoria.EN_REVISION, historia.getEstado());
        }

        @Test
        void debeFallarSiNoEsBorrador() {
            HistoriaClinica historia = crearHistoriaBorrador();
            historia.pasarARevision();
            assertThrows(HistoriaClinicaException.class, historia::pasarARevision);
        }
    }

    @Nested
    @DisplayName("anular()")
    class Anular {

        @Test
        void debeAnularDesdeBorrador() {
            HistoriaClinica historia = crearHistoriaBorrador();
            historia.anular();
            assertEquals(EstadoHistoria.ANULADA, historia.getEstado());
        }

        @Test
        void debeFallarSiYaAnulada() {
            HistoriaClinica historia = crearHistoriaBorrador();
            historia.anular();
            assertThrows(HistoriaClinicaException.class, historia::anular);
        }
    }

    @Nested
    @DisplayName("crearNuevaVersion()")
    class CrearNuevaVersion {

        @Test
        void debeCrearNuevaVersionDesdeFinalizada() {
            HistoriaClinica historia = crearHistoriaBorrador();
            historia.finalizar();

            HistoriaClinica nueva = historia.crearNuevaVersion();

            assertNotEquals(historia.getId(), nueva.getId());
            assertEquals(historia.getPacienteId(), nueva.getPacienteId());
            assertEquals(historia.getVersion() + 1, nueva.getVersion());
            assertEquals(EstadoHistoria.BORRADOR, nueva.getEstado());
        }

        @Test
        void debeFallarSiNoFinalizada() {
            HistoriaClinica historia = crearHistoriaBorrador();
            assertThrows(HistoriaClinicaException.class, historia::crearNuevaVersion);
        }
    }

    @Nested
    @DisplayName("validarOwnership()")
    class ValidarOwnership {

        @Test
        void debePermitirAlOwner() {
            HistoriaClinica historia = crearHistoriaBorrador();
            assertDoesNotThrow(() -> historia.validarOwnership(historia.getObstetraId()));
        }

        @Test
        void debeFallarConOtroObstetra() {
            HistoriaClinica historia = crearHistoriaBorrador();
            assertThrows(HistoriaClinicaException.class,
                    () -> historia.validarOwnership(UUID.randomUUID()));
        }
    }
}
