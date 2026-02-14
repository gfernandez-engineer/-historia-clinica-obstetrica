package com.clinica.transcripcion.application.service;

import com.clinica.transcripcion.domain.exception.TranscripcionNotFoundException;
import com.clinica.transcripcion.domain.model.EstadoTranscripcion;
import com.clinica.transcripcion.domain.model.OrigenTranscripcion;
import com.clinica.transcripcion.domain.model.Transcripcion;
import com.clinica.transcripcion.domain.port.in.ObtenerTranscripcionUseCase;
import com.clinica.transcripcion.domain.port.in.ProcesarAudioUseCase;
import com.clinica.transcripcion.domain.port.in.ProcesarTextoUseCase;
import com.clinica.transcripcion.domain.port.out.NormalizadorMedicoPort;
import com.clinica.transcripcion.domain.port.out.SpeechToTextPort;
import com.clinica.transcripcion.domain.port.out.TranscripcionEventPublisherPort;
import com.clinica.transcripcion.domain.port.out.TranscripcionRepositoryPort;
import com.clinica.shared.domain.event.AuditableEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranscripcionService implements ProcesarTextoUseCase, ProcesarAudioUseCase, ObtenerTranscripcionUseCase {

    private final TranscripcionRepositoryPort transcripcionRepository;
    private final TranscripcionEventPublisherPort eventPublisher;
    private final NormalizadorMedicoPort normalizadorMedico;
    private final SpeechToTextPort speechToText;

    @Override
    @Transactional
    public Transcripcion procesar(ProcesarTextoCommand command) {
        Transcripcion transcripcion = Transcripcion.builder()
                .id(UUID.randomUUID())
                .historiaClinicaId(command.historiaClinicaId())
                .obstetraId(command.obstetraId())
                .textoOriginal(command.texto())
                .estado(EstadoTranscripcion.PROCESANDO)
                .origen(OrigenTranscripcion.WEB_SPEECH_API)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        try {
            String textoNormalizado = normalizadorMedico.normalizar(command.texto());
            transcripcion.completar(textoNormalizado);
        } catch (Exception e) {
            log.error("Error normalizando texto: {}", e.getMessage(), e);
            transcripcion.marcarError(e.getMessage());
        }

        Transcripcion saved = transcripcionRepository.save(transcripcion);

        if (saved.getEstado() == EstadoTranscripcion.COMPLETADA) {
            publishCompletadaEvent(saved);
        }

        return saved;
    }

    @Override
    @Transactional
    public Transcripcion procesar(ProcesarAudioCommand command) {
        Transcripcion transcripcion = Transcripcion.builder()
                .id(UUID.randomUUID())
                .historiaClinicaId(command.historiaClinicaId())
                .obstetraId(command.obstetraId())
                .estado(EstadoTranscripcion.PROCESANDO)
                .origen(OrigenTranscripcion.AUDIO_UPLOAD)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        try {
            String textoTranscrito = speechToText.transcribir(command.audioData(), command.contentType());
            transcripcion = Transcripcion.builder()
                    .id(transcripcion.getId())
                    .historiaClinicaId(transcripcion.getHistoriaClinicaId())
                    .obstetraId(transcripcion.getObstetraId())
                    .textoOriginal(textoTranscrito)
                    .estado(EstadoTranscripcion.PROCESANDO)
                    .origen(OrigenTranscripcion.AUDIO_UPLOAD)
                    .createdAt(transcripcion.getCreatedAt())
                    .updatedAt(Instant.now())
                    .build();

            String textoNormalizado = normalizadorMedico.normalizar(textoTranscrito);
            transcripcion.completar(textoNormalizado);
        } catch (Exception e) {
            log.error("Error procesando audio: {}", e.getMessage(), e);
            transcripcion.marcarError(e.getMessage());
        }

        Transcripcion saved = transcripcionRepository.save(transcripcion);

        if (saved.getEstado() == EstadoTranscripcion.COMPLETADA) {
            publishCompletadaEvent(saved);
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Transcripcion obtenerPorId(UUID id, UUID obstetraId) {
        Transcripcion transcripcion = transcripcionRepository.findById(id)
                .orElseThrow(() -> new TranscripcionNotFoundException(id));

        if (!transcripcion.getObstetraId().equals(obstetraId)) {
            throw new TranscripcionNotFoundException(id);
        }

        return transcripcion;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Transcripcion> listarPorHistoria(UUID historiaClinicaId, UUID obstetraId, Pageable pageable) {
        return transcripcionRepository.findByHistoriaClinicaId(historiaClinicaId, pageable);
    }

    private void publishCompletadaEvent(Transcripcion transcripcion) {
        eventPublisher.publish(AuditableEvent.create(
                "transcripcion.completada",
                transcripcion.getObstetraId(),
                null,
                "TRANSCRIBIR",
                "TRANSCRIPCION",
                transcripcion.getId(),
                null,
                null,
                null,
                "ms-transcripcion"
        ));
    }
}
