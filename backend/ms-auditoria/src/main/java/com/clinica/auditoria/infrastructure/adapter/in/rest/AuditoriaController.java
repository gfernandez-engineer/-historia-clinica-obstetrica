package com.clinica.auditoria.infrastructure.adapter.in.rest;

import com.clinica.auditoria.domain.model.RegistroAuditoria;
import com.clinica.auditoria.domain.port.in.ConsultarAuditoriaUseCase;
import com.clinica.auditoria.domain.port.in.ConsultarAuditoriaUseCase.FiltroAuditoria;
import com.clinica.auditoria.infrastructure.adapter.in.rest.dto.RegistroAuditoriaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
@Tag(name = "Auditoria", description = "Consulta de registros de auditoria (solo AUDITOR y ADMIN)")
public class AuditoriaController {

    private final ConsultarAuditoriaUseCase consultarAuditoriaUseCase;

    @GetMapping
    @Operation(summary = "Consultar registros de auditoria",
            description = "Busca registros con filtros opcionales por usuario, recurso, tipo, accion y rango de fechas")
    @ApiResponse(responseCode = "200", description = "Lista paginada de registros")
    @ApiResponse(responseCode = "403", description = "No tiene permisos de auditor")
    public ResponseEntity<Page<RegistroAuditoriaResponse>> consultar(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID resourceId,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Instant desde,
            @RequestParam(required = false) Instant hasta,
            Pageable pageable) {

        FiltroAuditoria filtro = new FiltroAuditoria(
                userId, resourceId, resourceType, action, desde, hasta);

        Page<RegistroAuditoriaResponse> page = consultarAuditoriaUseCase
                .consultar(filtro, pageable)
                .map(this::toResponse);

        return ResponseEntity.ok(page);
    }

    private RegistroAuditoriaResponse toResponse(RegistroAuditoria registro) {
        return new RegistroAuditoriaResponse(
                registro.getId(),
                registro.getEventId(),
                registro.getOccurredOn(),
                registro.getEventType(),
                registro.getUserId(),
                registro.getUserEmail(),
                registro.getAction(),
                registro.getResourceType(),
                registro.getResourceId(),
                registro.getPreviousValue(),
                registro.getNewValue(),
                registro.getSourceIp(),
                registro.getSourceService(),
                registro.getReceivedAt()
        );
    }
}
