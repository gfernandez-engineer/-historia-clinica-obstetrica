package com.clinica.exportacion.application.service;

import com.clinica.exportacion.domain.exception.ExportacionException;
import com.clinica.exportacion.domain.model.DatosHistoriaClinica;
import com.clinica.exportacion.domain.model.ExportJob;
import com.clinica.exportacion.domain.port.in.DescargarPdfUseCase;
import com.clinica.exportacion.domain.port.in.GenerarExportacionUseCase;
import com.clinica.exportacion.domain.port.in.ListarExportacionesUseCase;
import com.clinica.exportacion.domain.port.in.ObtenerExportacionUseCase;
import com.clinica.exportacion.domain.port.out.ExportJobRepositoryPort;
import com.clinica.exportacion.domain.port.out.ExportacionEventPublisherPort;
import com.clinica.exportacion.domain.port.out.HistoriaClinicaClientPort;
import com.clinica.exportacion.domain.port.out.PdfGeneratorPort;
import com.clinica.shared.domain.event.AuditableEvent;
import com.clinica.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportacionService implements GenerarExportacionUseCase, ObtenerExportacionUseCase,
        ListarExportacionesUseCase, DescargarPdfUseCase {

    private final ExportJobRepositoryPort exportJobRepository;
    private final PdfGeneratorPort pdfGenerator;
    private final HistoriaClinicaClientPort historiaClinicaClient;
    private final ExportacionEventPublisherPort eventPublisher;

    @Override
    @Transactional
    public ExportJob generar(GenerarExportacionUseCase.Command command) {
        ExportJob job = ExportJob.crear(command.historiaClinicaId(), command.obstetraId(), command.formato());
        job.marcarProcesando();
        job = exportJobRepository.save(job);

        try {
            DatosHistoriaClinica datos = historiaClinicaClient.obtenerHistoriaCompleta(
                    command.historiaClinicaId(), command.jwtToken());

            byte[] pdfBytes = pdfGenerator.generar(datos);

            job.marcarCompletado("export-" + job.getId() + ".pdf");
            job = exportJobRepository.save(job);

            log.info("PDF generado exitosamente para historia {} - job {}", command.historiaClinicaId(), job.getId());

            eventPublisher.publish(AuditableEvent.create(
                    "exportacion.pdf.generado",
                    command.obstetraId(),
                    null,
                    "exportacion.pdf.generado",
                    "ExportJob",
                    job.getId(),
                    null,
                    job.getArchivoUrl(),
                    null,
                    "ms-exportacion"
            ));

            return job;
        } catch (Exception e) {
            log.error("Error generando PDF para historia {}: {}", command.historiaClinicaId(), e.getMessage(), e);
            job.marcarError(e.getMessage());
            exportJobRepository.save(job);
            throw new ExportacionException("Error generando PDF: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ExportJob obtener(UUID exportJobId, UUID obstetraId) {
        return exportJobRepository.findById(exportJobId)
                .filter(job -> job.getObstetraId().equals(obstetraId))
                .orElseThrow(() -> new ResourceNotFoundException("ExportJob", exportJobId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExportJob> listarPorObstetra(UUID obstetraId, Pageable pageable) {
        return exportJobRepository.findByObstetraId(obstetraId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] descargar(UUID exportJobId, UUID obstetraId, String jwtToken) {
        ExportJob job = obtener(exportJobId, obstetraId);

        if (job.getEstado() != com.clinica.exportacion.domain.model.EstadoExportacion.COMPLETADO) {
            throw new ExportacionException("La exportacion aun no esta completada");
        }

        DatosHistoriaClinica datos = historiaClinicaClient.obtenerHistoriaCompleta(
                job.getHistoriaClinicaId(), jwtToken);

        return pdfGenerator.generar(datos);
    }
}
