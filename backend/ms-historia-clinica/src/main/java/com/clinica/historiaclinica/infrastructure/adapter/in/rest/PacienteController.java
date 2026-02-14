package com.clinica.historiaclinica.infrastructure.adapter.in.rest;

import com.clinica.historiaclinica.domain.model.Paciente;
import com.clinica.historiaclinica.domain.port.in.ActualizarPacienteUseCase;
import com.clinica.historiaclinica.domain.port.in.CrearPacienteUseCase;
import com.clinica.historiaclinica.domain.port.in.ListarPacientesUseCase;
import com.clinica.historiaclinica.domain.port.in.ObtenerPacienteUseCase;
import com.clinica.historiaclinica.infrastructure.adapter.in.rest.dto.ActualizarPacienteRequest;
import com.clinica.historiaclinica.infrastructure.adapter.in.rest.dto.CrearPacienteRequest;
import com.clinica.historiaclinica.infrastructure.adapter.in.rest.dto.PacienteResponse;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
@Tag(name = "Pacientes", description = "Gestion de pacientes obstetricos")
public class PacienteController {

    private final CrearPacienteUseCase crearPacienteUseCase;
    private final ObtenerPacienteUseCase obtenerPacienteUseCase;
    private final ListarPacientesUseCase listarPacientesUseCase;
    private final ActualizarPacienteUseCase actualizarPacienteUseCase;

    @PostMapping
    @Operation(summary = "Crear paciente", description = "Registra un nuevo paciente asociado al obstetra autenticado")
    @ApiResponse(responseCode = "201", description = "Paciente creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos invalidos o DNI duplicado")
    public ResponseEntity<PacienteResponse> crear(
            @Valid @RequestBody CrearPacienteRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {

        Paciente paciente = crearPacienteUseCase.crear(
                new CrearPacienteUseCase.CrearPacienteCommand(
                        request.dni(),
                        request.nombre(),
                        request.apellido(),
                        request.fechaNacimiento(),
                        request.telefono(),
                        request.direccion(),
                        user.userId()
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(paciente));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener paciente", description = "Obtiene un paciente por su ID")
    @ApiResponse(responseCode = "200", description = "Paciente encontrado")
    @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    public ResponseEntity<PacienteResponse> obtener(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user) {

        Paciente paciente = obtenerPacienteUseCase.obtenerPorId(id, user.userId());
        return ResponseEntity.ok(toResponse(paciente));
    }

    @GetMapping
    @Operation(summary = "Listar pacientes", description = "Lista pacientes del obstetra autenticado con paginacion")
    @ApiResponse(responseCode = "200", description = "Lista de pacientes")
    public ResponseEntity<Page<PacienteResponse>> listar(
            @RequestParam(required = false) String buscar,
            Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser user) {

        Page<PacienteResponse> page;
        if (buscar != null && !buscar.isBlank()) {
            page = listarPacientesUseCase
                    .buscarPorNombreODni(user.userId(), buscar, pageable)
                    .map(this::toResponse);
        } else {
            page = listarPacientesUseCase
                    .listarPorObstetra(user.userId(), pageable)
                    .map(this::toResponse);
        }

        return ResponseEntity.ok(page);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar paciente", description = "Actualiza los datos de un paciente existente")
    @ApiResponse(responseCode = "200", description = "Paciente actualizado")
    @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    public ResponseEntity<PacienteResponse> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarPacienteRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {

        Paciente paciente = actualizarPacienteUseCase.actualizar(
                new ActualizarPacienteUseCase.ActualizarPacienteCommand(
                        id,
                        request.nombre(),
                        request.apellido(),
                        request.fechaNacimiento(),
                        request.telefono(),
                        request.direccion(),
                        user.userId()
                )
        );

        return ResponseEntity.ok(toResponse(paciente));
    }

    private PacienteResponse toResponse(Paciente paciente) {
        return new PacienteResponse(
                paciente.getId(),
                paciente.getDni(),
                paciente.getNombre(),
                paciente.getApellido(),
                paciente.getFechaNacimiento(),
                paciente.getTelefono(),
                paciente.getDireccion(),
                paciente.getCreatedAt(),
                paciente.getUpdatedAt()
        );
    }
}
