package com.clinica.historiaclinica.infrastructure.adapter.in.rest;

import com.clinica.historiaclinica.domain.model.*;
import com.clinica.historiaclinica.domain.port.in.*;
import com.clinica.historiaclinica.infrastructure.adapter.in.rest.dto.*;
import com.clinica.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/historias-clinicas")
@RequiredArgsConstructor
@Tag(name = "Historias Clinicas", description = "Gestion de historias clinicas obstétricas")
public class HistoriaClinicaController {

    private final CrearHistoriaClinicaUseCase crearHistoriaUseCase;
    private final ObtenerHistoriaClinicaUseCase obtenerHistoriaUseCase;
    private final ActualizarHistoriaClinicaUseCase actualizarHistoriaUseCase;
    private final CambiarEstadoHistoriaUseCase cambiarEstadoUseCase;
    private final ListarHistoriasClinicasUseCase listarHistoriasUseCase;

    @PostMapping
    @Operation(summary = "Crear historia clinica", description = "Crea una nueva historia clinica en estado BORRADOR")
    @ApiResponse(responseCode = "201", description = "Historia clinica creada")
    @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    public ResponseEntity<HistoriaClinicaResponse> crear(
            @Valid @RequestBody CrearHistoriaRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {

        HistoriaClinica historia = crearHistoriaUseCase.crear(
                new CrearHistoriaClinicaUseCase.CrearHistoriaCommand(
                        request.pacienteId(),
                        user.userId(),
                        request.notasGenerales()
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(historia));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener historia clinica", description = "Obtiene una historia clinica con todas sus secciones")
    @ApiResponse(responseCode = "200", description = "Historia clinica encontrada")
    @ApiResponse(responseCode = "404", description = "Historia clinica no encontrada")
    public ResponseEntity<HistoriaClinicaResponse> obtener(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user) {

        HistoriaClinica historia = obtenerHistoriaUseCase.obtenerPorId(id, user.userId());
        return ResponseEntity.ok(toResponse(historia));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar historia clinica", description = "Actualiza secciones, eventos y medicamentos")
    @ApiResponse(responseCode = "200", description = "Historia clinica actualizada")
    @ApiResponse(responseCode = "400", description = "Historia no editable")
    @ApiResponse(responseCode = "404", description = "Historia clinica no encontrada")
    public ResponseEntity<HistoriaClinicaResponse> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarHistoriaRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {

        List<ActualizarHistoriaClinicaUseCase.SeccionCommand> secciones = request.secciones() != null
                ? request.secciones().stream()
                    .map(s -> new ActualizarHistoriaClinicaUseCase.SeccionCommand(
                            s.tipo(), s.contenido(), s.origen(), s.orden()))
                    .toList()
                : null;

        List<ActualizarHistoriaClinicaUseCase.EventoCommand> eventos = request.eventos() != null
                ? request.eventos().stream()
                    .map(e -> new ActualizarHistoriaClinicaUseCase.EventoCommand(
                            e.tipo(), e.fecha(), e.semanaGestacional(), e.observaciones()))
                    .toList()
                : null;

        List<ActualizarHistoriaClinicaUseCase.MedicamentoCommand> medicamentos = request.medicamentos() != null
                ? request.medicamentos().stream()
                    .map(m -> new ActualizarHistoriaClinicaUseCase.MedicamentoCommand(
                            m.nombre(), m.dosis(), m.via(), m.frecuencia(), m.duracion()))
                    .toList()
                : null;

        HistoriaClinica historia = actualizarHistoriaUseCase.actualizar(
                new ActualizarHistoriaClinicaUseCase.ActualizarHistoriaCommand(
                        id,
                        user.userId(),
                        request.notasGenerales(),
                        secciones,
                        eventos,
                        medicamentos
                )
        );

        return ResponseEntity.ok(toResponse(historia));
    }

    @GetMapping
    @Operation(summary = "Listar historias clinicas", description = "Lista historias del obstetra, opcionalmente filtradas por paciente")
    @ApiResponse(responseCode = "200", description = "Lista de historias clinicas")
    public ResponseEntity<Page<HistoriaClinicaResponse>> listar(
            @RequestParam(required = false) UUID pacienteId,
            Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser user) {

        Page<HistoriaClinicaResponse> page;
        if (pacienteId != null) {
            page = listarHistoriasUseCase
                    .listarPorPaciente(pacienteId, user.userId(), pageable)
                    .map(this::toResponse);
        } else {
            page = listarHistoriasUseCase
                    .listarPorObstetra(user.userId(), pageable)
                    .map(this::toResponse);
        }

        return ResponseEntity.ok(page);
    }

    @PatchMapping("/{id}/finalizar")
    @Operation(summary = "Finalizar historia clinica", description = "Cambia el estado a FINALIZADA (inmutable)")
    @ApiResponse(responseCode = "200", description = "Historia clinica finalizada")
    @ApiResponse(responseCode = "400", description = "No se puede finalizar")
    public ResponseEntity<HistoriaClinicaResponse> finalizar(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user) {

        HistoriaClinica historia = cambiarEstadoUseCase.finalizar(id, user.userId());
        return ResponseEntity.ok(toResponse(historia));
    }

    @PatchMapping("/{id}/revision")
    @Operation(summary = "Pasar a revision", description = "Cambia el estado a EN_REVISION")
    @ApiResponse(responseCode = "200", description = "Historia en revision")
    @ApiResponse(responseCode = "400", description = "No se puede pasar a revision")
    public ResponseEntity<HistoriaClinicaResponse> pasarARevision(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user) {

        HistoriaClinica historia = cambiarEstadoUseCase.pasarARevision(id, user.userId());
        return ResponseEntity.ok(toResponse(historia));
    }

    @PatchMapping("/{id}/anular")
    @Operation(summary = "Anular historia clinica", description = "Cambia el estado a ANULADA")
    @ApiResponse(responseCode = "200", description = "Historia clinica anulada")
    @ApiResponse(responseCode = "400", description = "No se puede anular")
    public ResponseEntity<HistoriaClinicaResponse> anular(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user) {

        HistoriaClinica historia = cambiarEstadoUseCase.anular(id, user.userId());
        return ResponseEntity.ok(toResponse(historia));
    }

    private HistoriaClinicaResponse toResponse(HistoriaClinica historia) {
        List<HistoriaClinicaResponse.SeccionResponse> secciones = historia.getSecciones() != null
                ? historia.getSecciones().stream()
                    .map(s -> new HistoriaClinicaResponse.SeccionResponse(
                            s.getId(), s.getTipo(), s.getContenido(), s.getOrigen(), s.getOrden()))
                    .toList()
                : List.of();

        List<HistoriaClinicaResponse.EventoResponse> eventos = historia.getEventos() != null
                ? historia.getEventos().stream()
                    .map(e -> new HistoriaClinicaResponse.EventoResponse(
                            e.getId(), e.getTipo(), e.getFecha(), e.getSemanaGestacional(), e.getObservaciones()))
                    .toList()
                : List.of();

        List<HistoriaClinicaResponse.MedicamentoResponse> medicamentos = historia.getMedicamentos() != null
                ? historia.getMedicamentos().stream()
                    .map(m -> new HistoriaClinicaResponse.MedicamentoResponse(
                            m.getId(), m.getNombre(), m.getDosis(), m.getVia(), m.getFrecuencia(), m.getDuracion()))
                    .toList()
                : List.of();

        return new HistoriaClinicaResponse(
                historia.getId(),
                historia.getPacienteId(),
                historia.getVersion(),
                historia.getEstado(),
                historia.getNotasGenerales(),
                secciones,
                eventos,
                medicamentos,
                historia.getCreatedAt(),
                historia.getUpdatedAt()
        );
    }
}
