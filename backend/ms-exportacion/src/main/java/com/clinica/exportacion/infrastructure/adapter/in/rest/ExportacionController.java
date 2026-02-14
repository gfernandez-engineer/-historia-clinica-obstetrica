package com.clinica.exportacion.infrastructure.adapter.in.rest;

import com.clinica.exportacion.domain.model.ExportJob;
import com.clinica.exportacion.domain.port.in.DescargarPdfUseCase;
import com.clinica.exportacion.domain.port.in.GenerarExportacionUseCase;
import com.clinica.exportacion.domain.port.in.ListarExportacionesUseCase;
import com.clinica.exportacion.domain.port.in.ObtenerExportacionUseCase;
import com.clinica.exportacion.infrastructure.adapter.in.rest.dto.ExportJobResponse;
import com.clinica.exportacion.infrastructure.adapter.in.rest.dto.GenerarExportacionRequest;
import com.clinica.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/exportaciones")
@RequiredArgsConstructor
@Tag(name = "Exportaciones", description = "Generacion y descarga de PDFs de historias clinicas")
public class ExportacionController {

    private final GenerarExportacionUseCase generarExportacionUseCase;
    private final ObtenerExportacionUseCase obtenerExportacionUseCase;
    private final ListarExportacionesUseCase listarExportacionesUseCase;
    private final DescargarPdfUseCase descargarPdfUseCase;

    @PostMapping
    @Operation(summary = "Generar exportacion PDF de una historia clinica")
    @ApiResponse(responseCode = "200", description = "Exportacion generada exitosamente")
    public ResponseEntity<ExportJobResponse> generar(
            @Valid @RequestBody GenerarExportacionRequest request,
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        String jwtToken = authHeader != null && authHeader.startsWith("Bearer ")
                ? authHeader.substring(7) : null;

        GenerarExportacionUseCase.Command command = new GenerarExportacionUseCase.Command(
                request.historiaClinicaId(),
                user.userId(),
                request.formato(),
                jwtToken
        );

        ExportJob job = generarExportacionUseCase.generar(command);
        return ResponseEntity.ok(toResponse(job));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener estado de una exportacion")
    @ApiResponse(responseCode = "200", description = "Exportacion encontrada")
    public ResponseEntity<ExportJobResponse> obtener(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user) {

        ExportJob job = obtenerExportacionUseCase.obtener(id, user.userId());
        return ResponseEntity.ok(toResponse(job));
    }

    @GetMapping
    @Operation(summary = "Listar exportaciones del obstetra autenticado")
    @ApiResponse(responseCode = "200", description = "Listado de exportaciones")
    public ResponseEntity<Page<ExportJobResponse>> listar(
            @AuthenticationPrincipal AuthenticatedUser user,
            Pageable pageable) {

        Page<ExportJobResponse> page = listarExportacionesUseCase
                .listarPorObstetra(user.userId(), pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}/descargar")
    @Operation(summary = "Descargar PDF de una exportacion completada")
    @ApiResponse(responseCode = "200", description = "PDF descargado")
    public ResponseEntity<byte[]> descargar(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        String jwtToken = authHeader != null && authHeader.startsWith("Bearer ")
                ? authHeader.substring(7) : null;
        byte[] pdfBytes = descargarPdfUseCase.descargar(id, user.userId(), jwtToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=historia-clinica-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }

    private ExportJobResponse toResponse(ExportJob job) {
        return new ExportJobResponse(
                job.getId(),
                job.getHistoriaClinicaId(),
                job.getFormato(),
                job.getEstado(),
                job.getArchivoUrl(),
                job.getErrorMensaje(),
                job.getCreatedAt(),
                job.getCompletedAt()
        );
    }
}
