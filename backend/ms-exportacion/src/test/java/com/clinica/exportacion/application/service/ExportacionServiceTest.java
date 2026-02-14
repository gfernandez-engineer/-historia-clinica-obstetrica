package com.clinica.exportacion.application.service;

import com.clinica.exportacion.domain.exception.ExportacionException;
import com.clinica.exportacion.domain.model.*;
import com.clinica.exportacion.domain.port.in.GenerarExportacionUseCase;
import com.clinica.exportacion.domain.port.out.ExportJobRepositoryPort;
import com.clinica.exportacion.domain.port.out.ExportacionEventPublisherPort;
import com.clinica.exportacion.domain.port.out.HistoriaClinicaClientPort;
import com.clinica.exportacion.domain.port.out.PdfGeneratorPort;
import com.clinica.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportacionServiceTest {

    @Mock
    private ExportJobRepositoryPort exportJobRepository;

    @Mock
    private PdfGeneratorPort pdfGenerator;

    @Mock
    private HistoriaClinicaClientPort historiaClinicaClient;

    @Mock
    private ExportacionEventPublisherPort eventPublisher;

    @InjectMocks
    private ExportacionService exportacionService;

    private final UUID obstetraId = UUID.randomUUID();
    private final UUID historiaClinicaId = UUID.randomUUID();

    @Test
    @DisplayName("Debe generar exportacion PDF exitosamente")
    void debeGenerarExportacionExitosamente() {
        DatosHistoriaClinica datos = crearDatosHistoria();
        byte[] pdfBytes = "pdf-content".getBytes();

        when(historiaClinicaClient.obtenerHistoriaCompleta(eq(historiaClinicaId), any()))
                .thenReturn(datos);
        when(pdfGenerator.generar(datos)).thenReturn(pdfBytes);
        when(exportJobRepository.save(any(ExportJob.class))).thenAnswer(inv -> inv.getArgument(0));

        GenerarExportacionUseCase.Command command = new GenerarExportacionUseCase.Command(
                historiaClinicaId, obstetraId, FormatoExportacion.PDF, "test-token");

        ExportJob result = exportacionService.generar(command);

        assertNotNull(result);
        assertEquals(EstadoExportacion.COMPLETADO, result.getEstado());
        assertNotNull(result.getArchivoUrl());
        verify(exportJobRepository, times(2)).save(any(ExportJob.class));
        verify(eventPublisher).publish(any());
    }

    @Test
    @DisplayName("Debe marcar error cuando falla la generacion de PDF")
    void debeMarcarErrorCuandoFallaGeneracion() {
        when(historiaClinicaClient.obtenerHistoriaCompleta(eq(historiaClinicaId), any()))
                .thenThrow(new RuntimeException("Error de conexion"));
        when(exportJobRepository.save(any(ExportJob.class))).thenAnswer(inv -> inv.getArgument(0));

        GenerarExportacionUseCase.Command command = new GenerarExportacionUseCase.Command(
                historiaClinicaId, obstetraId, FormatoExportacion.PDF, "test-token");

        assertThrows(ExportacionException.class, () -> exportacionService.generar(command));
        verify(exportJobRepository, times(2)).save(any(ExportJob.class));
    }

    @Test
    @DisplayName("Debe obtener exportacion por ID y obstetra")
    void debeObtenerExportacionPorId() {
        ExportJob job = crearExportJob(EstadoExportacion.COMPLETADO);
        when(exportJobRepository.findById(job.getId())).thenReturn(Optional.of(job));

        ExportJob result = exportacionService.obtener(job.getId(), obstetraId);

        assertNotNull(result);
        assertEquals(job.getId(), result.getId());
    }

    @Test
    @DisplayName("Debe lanzar NotFoundException si exportacion no pertenece al obstetra")
    void debeLanzarNotFoundSiNoEsOwner() {
        ExportJob job = crearExportJob(EstadoExportacion.COMPLETADO);
        when(exportJobRepository.findById(job.getId())).thenReturn(Optional.of(job));

        UUID otroObstetra = UUID.randomUUID();
        assertThrows(ResourceNotFoundException.class,
                () -> exportacionService.obtener(job.getId(), otroObstetra));
    }

    @Test
    @DisplayName("Debe lanzar NotFoundException si exportacion no existe")
    void debeLanzarNotFoundSiNoExiste() {
        UUID jobId = UUID.randomUUID();
        when(exportJobRepository.findById(jobId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> exportacionService.obtener(jobId, obstetraId));
    }

    @Test
    @DisplayName("Debe listar exportaciones del obstetra con paginacion")
    void debeListarExportaciones() {
        Pageable pageable = PageRequest.of(0, 10);
        ExportJob job = crearExportJob(EstadoExportacion.COMPLETADO);
        Page<ExportJob> page = new PageImpl<>(List.of(job), pageable, 1);

        when(exportJobRepository.findByObstetraId(obstetraId, pageable)).thenReturn(page);

        Page<ExportJob> result = exportacionService.listarPorObstetra(obstetraId, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(job.getId(), result.getContent().get(0).getId());
    }

    @Test
    @DisplayName("Debe descargar PDF de exportacion completada")
    void debeDescargarPdf() {
        ExportJob job = crearExportJob(EstadoExportacion.COMPLETADO);
        DatosHistoriaClinica datos = crearDatosHistoria();
        byte[] pdfBytes = "pdf-content".getBytes();

        when(exportJobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(historiaClinicaClient.obtenerHistoriaCompleta(eq(historiaClinicaId), any()))
                .thenReturn(datos);
        when(pdfGenerator.generar(datos)).thenReturn(pdfBytes);

        byte[] result = exportacionService.descargar(job.getId(), obstetraId, "test-token");

        assertNotNull(result);
        assertEquals(pdfBytes.length, result.length);
    }

    @Test
    @DisplayName("Debe lanzar excepcion al descargar exportacion no completada")
    void debeLanzarExcepcionSiNoCompletada() {
        ExportJob job = crearExportJob(EstadoExportacion.PROCESANDO);
        when(exportJobRepository.findById(job.getId())).thenReturn(Optional.of(job));

        assertThrows(ExportacionException.class,
                () -> exportacionService.descargar(job.getId(), obstetraId, "test-token"));
    }

    private ExportJob crearExportJob(EstadoExportacion estado) {
        return ExportJob.builder()
                .id(UUID.randomUUID())
                .historiaClinicaId(historiaClinicaId)
                .obstetraId(obstetraId)
                .formato(FormatoExportacion.PDF)
                .estado(estado)
                .archivoUrl(estado == EstadoExportacion.COMPLETADO ? "export-test.pdf" : null)
                .createdAt(Instant.now())
                .build();
    }

    private DatosHistoriaClinica crearDatosHistoria() {
        return DatosHistoriaClinica.builder()
                .id(historiaClinicaId)
                .version(1)
                .estado("FINALIZADA")
                .notasGenerales("Notas de prueba")
                .pacienteNombre("Maria")
                .pacienteApellido("Garcia")
                .pacienteDni("12345678")
                .secciones(List.of(
                        DatosHistoriaClinica.Seccion.builder()
                                .tipo("DATOS_INGRESO")
                                .contenido("Datos de ingreso de prueba")
                                .orden(1)
                                .build()
                ))
                .eventos(List.of(
                        DatosHistoriaClinica.Evento.builder()
                                .tipo("Control prenatal")
                                .fecha(Instant.now())
                                .semanaGestacional(20)
                                .observaciones("Control normal")
                                .build()
                ))
                .medicamentos(List.of(
                        DatosHistoriaClinica.Medicamento.builder()
                                .nombre("Acido folico")
                                .dosis("5mg")
                                .via("oral")
                                .frecuencia("1 vez al dia")
                                .duracion("3 meses")
                                .build()
                ))
                .build();
    }
}
