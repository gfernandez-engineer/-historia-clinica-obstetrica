package com.clinica.transcripcion.infrastructure.adapter.in.rest;

import com.clinica.shared.security.AuthenticatedUser;
import com.clinica.transcripcion.domain.model.Transcripcion;
import com.clinica.transcripcion.domain.port.in.ObtenerTranscripcionUseCase;
import com.clinica.transcripcion.domain.port.in.ProcesarAudioUseCase;
import com.clinica.transcripcion.domain.port.in.ProcesarTextoUseCase;
import com.clinica.transcripcion.infrastructure.adapter.in.rest.dto.ProcesarTextoRequest;
import com.clinica.transcripcion.infrastructure.adapter.in.rest.dto.TranscripcionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/transcripciones")
@RequiredArgsConstructor
@Tag(name = "Transcripciones", description = "Procesamiento de texto y audio clínico")
public class TranscripcionController {

    private final ProcesarTextoUseCase procesarTextoUseCase;
    private final ProcesarAudioUseCase procesarAudioUseCase;
    private final ObtenerTranscripcionUseCase obtenerTranscripcionUseCase;

    @PostMapping("/texto")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Procesar texto de Web Speech API")
    public TranscripcionResponse procesarTexto(
            @Valid @RequestBody ProcesarTextoRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {

        Transcripcion result = procesarTextoUseCase.procesar(
                new ProcesarTextoUseCase.ProcesarTextoCommand(
                        request.historiaClinicaId(),
                        user.userId(),
                        request.texto()
                )
        );
        return TranscripcionResponse.fromDomain(result);
    }

    @PostMapping(value = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Procesar archivo de audio (fallback)")
    public TranscripcionResponse procesarAudio(
            @RequestParam UUID historiaClinicaId,
            @RequestParam("archivo") MultipartFile archivo,
            @AuthenticationPrincipal AuthenticatedUser user) throws IOException {

        Transcripcion result = procesarAudioUseCase.procesar(
                new ProcesarAudioUseCase.ProcesarAudioCommand(
                        historiaClinicaId,
                        user.userId(),
                        archivo.getBytes(),
                        archivo.getContentType()
                )
        );
        return TranscripcionResponse.fromDomain(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener transcripción por ID")
    public TranscripcionResponse obtenerPorId(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user) {

        Transcripcion transcripcion = obtenerTranscripcionUseCase.obtenerPorId(id, user.userId());
        return TranscripcionResponse.fromDomain(transcripcion);
    }

    @GetMapping("/historia/{historiaClinicaId}")
    @Operation(summary = "Listar transcripciones de una historia clínica")
    public Page<TranscripcionResponse> listarPorHistoria(
            @PathVariable UUID historiaClinicaId,
            @AuthenticationPrincipal AuthenticatedUser user,
            Pageable pageable) {

        return obtenerTranscripcionUseCase.listarPorHistoria(historiaClinicaId, user.userId(), pageable)
                .map(TranscripcionResponse::fromDomain);
    }
}
