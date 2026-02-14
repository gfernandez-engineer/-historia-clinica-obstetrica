package com.clinica.transcripcion.application.service;

import com.clinica.transcripcion.domain.exception.TranscripcionNotFoundException;
import com.clinica.transcripcion.domain.model.EstadoTranscripcion;
import com.clinica.transcripcion.domain.model.OrigenTranscripcion;
import com.clinica.transcripcion.domain.model.Transcripcion;
import com.clinica.transcripcion.domain.port.in.ProcesarAudioUseCase;
import com.clinica.transcripcion.domain.port.in.ProcesarTextoUseCase;
import com.clinica.transcripcion.domain.port.out.NormalizadorMedicoPort;
import com.clinica.transcripcion.domain.port.out.SpeechToTextPort;
import com.clinica.transcripcion.domain.port.out.TranscripcionEventPublisherPort;
import com.clinica.transcripcion.domain.port.out.TranscripcionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TranscripcionServiceTest {

    @Mock
    private TranscripcionRepositoryPort transcripcionRepository;
    @Mock
    private TranscripcionEventPublisherPort eventPublisher;
    @Mock
    private NormalizadorMedicoPort normalizadorMedico;
    @Mock
    private SpeechToTextPort speechToText;

    @InjectMocks
    private TranscripcionService service;

    private UUID historiaId;
    private UUID obstetraId;

    @BeforeEach
    void setUp() {
        historiaId = UUID.randomUUID();
        obstetraId = UUID.randomUUID();
    }

    @Test
    void procesarTexto_debeNormalizarYGuardar() {
        String textoOriginal = "paciente con preeclampsia severa";
        String textoNormalizado = "paciente con preeclampsia (CIE-10: O14) severa";

        when(normalizadorMedico.normalizar(textoOriginal)).thenReturn(textoNormalizado);
        when(transcripcionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Transcripcion result = service.procesar(
                new ProcesarTextoUseCase.ProcesarTextoCommand(historiaId, obstetraId, textoOriginal)
        );

        assertThat(result.getEstado()).isEqualTo(EstadoTranscripcion.COMPLETADA);
        assertThat(result.getTextoOriginal()).isEqualTo(textoOriginal);
        assertThat(result.getTextoNormalizado()).isEqualTo(textoNormalizado);
        assertThat(result.getOrigen()).isEqualTo(OrigenTranscripcion.WEB_SPEECH_API);
        verify(eventPublisher).publish(any());
    }

    @Test
    void procesarTexto_conErrorNormalizacion_debeMarcarError() {
        when(normalizadorMedico.normalizar(any())).thenThrow(new RuntimeException("Error normalización"));
        when(transcripcionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Transcripcion result = service.procesar(
                new ProcesarTextoUseCase.ProcesarTextoCommand(historiaId, obstetraId, "texto")
        );

        assertThat(result.getEstado()).isEqualTo(EstadoTranscripcion.ERROR);
        assertThat(result.getErrorDetalle()).contains("Error normalización");
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void procesarAudio_debeTranscribirNormalizarYGuardar() {
        byte[] audio = new byte[]{1, 2, 3};
        String textoTranscrito = "texto del audio";
        String textoNormalizado = "texto normalizado del audio";

        when(speechToText.transcribir(audio, "audio/wav")).thenReturn(textoTranscrito);
        when(normalizadorMedico.normalizar(textoTranscrito)).thenReturn(textoNormalizado);
        when(transcripcionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Transcripcion result = service.procesar(
                new ProcesarAudioUseCase.ProcesarAudioCommand(historiaId, obstetraId, audio, "audio/wav")
        );

        assertThat(result.getEstado()).isEqualTo(EstadoTranscripcion.COMPLETADA);
        assertThat(result.getTextoOriginal()).isEqualTo(textoTranscrito);
        assertThat(result.getTextoNormalizado()).isEqualTo(textoNormalizado);
        assertThat(result.getOrigen()).isEqualTo(OrigenTranscripcion.AUDIO_UPLOAD);
        verify(eventPublisher).publish(any());
    }

    @Test
    void procesarAudio_conErrorSpeechToText_debeMarcarError() {
        when(speechToText.transcribir(any(), any())).thenThrow(new RuntimeException("STT error"));
        when(transcripcionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Transcripcion result = service.procesar(
                new ProcesarAudioUseCase.ProcesarAudioCommand(historiaId, obstetraId, new byte[]{1}, "audio/wav")
        );

        assertThat(result.getEstado()).isEqualTo(EstadoTranscripcion.ERROR);
        assertThat(result.getErrorDetalle()).contains("STT error");
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void obtenerPorId_conOwnerCorrecto_debeRetornar() {
        UUID transcripcionId = UUID.randomUUID();
        Transcripcion transcripcion = buildTranscripcion(transcripcionId, obstetraId);

        when(transcripcionRepository.findById(transcripcionId)).thenReturn(Optional.of(transcripcion));

        Transcripcion result = service.obtenerPorId(transcripcionId, obstetraId);

        assertThat(result.getId()).isEqualTo(transcripcionId);
    }

    @Test
    void obtenerPorId_conOwnerIncorrecto_debeLanzarExcepcion() {
        UUID transcripcionId = UUID.randomUUID();
        UUID otroObstetra = UUID.randomUUID();
        Transcripcion transcripcion = buildTranscripcion(transcripcionId, obstetraId);

        when(transcripcionRepository.findById(transcripcionId)).thenReturn(Optional.of(transcripcion));

        assertThatThrownBy(() -> service.obtenerPorId(transcripcionId, otroObstetra))
                .isInstanceOf(TranscripcionNotFoundException.class);
    }

    @Test
    void obtenerPorId_noExiste_debeLanzarExcepcion() {
        UUID transcripcionId = UUID.randomUUID();
        when(transcripcionRepository.findById(transcripcionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId(transcripcionId, obstetraId))
                .isInstanceOf(TranscripcionNotFoundException.class);
    }

    @Test
    void listarPorHistoria_debeRetornarPagina() {
        Transcripcion t = buildTranscripcion(UUID.randomUUID(), obstetraId);
        Page<Transcripcion> page = new PageImpl<>(List.of(t));
        when(transcripcionRepository.findByHistoriaClinicaId(historiaId, PageRequest.of(0, 10))).thenReturn(page);

        Page<Transcripcion> result = service.listarPorHistoria(historiaId, obstetraId, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    private Transcripcion buildTranscripcion(UUID id, UUID obstetra) {
        return Transcripcion.builder()
                .id(id)
                .historiaClinicaId(historiaId)
                .obstetraId(obstetra)
                .textoOriginal("texto original")
                .textoNormalizado("texto normalizado")
                .estado(EstadoTranscripcion.COMPLETADA)
                .origen(OrigenTranscripcion.WEB_SPEECH_API)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
