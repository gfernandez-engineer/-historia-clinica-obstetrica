package com.clinica.auditoria.application.service;

import com.clinica.auditoria.domain.model.RegistroAuditoria;
import com.clinica.auditoria.domain.port.in.ConsultarAuditoriaUseCase.FiltroAuditoria;
import com.clinica.auditoria.domain.port.out.AuditoriaRepositoryPort;
import com.clinica.shared.domain.event.AuditableEvent;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private AuditoriaRepositoryPort auditoriaRepository;

    @InjectMocks
    private AuditoriaService auditoriaService;

    @Test
    @DisplayName("Debe registrar un evento de auditoria correctamente")
    void debeRegistrarEvento() {
        AuditableEvent event = AuditableEvent.create(
                "auth.usuario.login",
                UUID.randomUUID(),
                "maria@clinica.com",
                "LOGIN",
                "USUARIO",
                UUID.randomUUID(),
                null,
                null,
                "192.168.1.1",
                "ms-auth"
        );

        when(auditoriaRepository.save(any(RegistroAuditoria.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RegistroAuditoria result = auditoriaService.registrar(event);

        assertNotNull(result.getId());
        assertEquals(event.getEventId(), result.getEventId());
        assertEquals("LOGIN", result.getAction());
        assertEquals("USUARIO", result.getResourceType());
        assertEquals("ms-auth", result.getSourceService());
        assertNotNull(result.getReceivedAt());
        verify(auditoriaRepository).save(any(RegistroAuditoria.class));
    }

    @Test
    @DisplayName("Debe consultar registros con filtros")
    void debeConsultarConFiltros() {
        UUID userId = UUID.randomUUID();
        FiltroAuditoria filtro = new FiltroAuditoria(
                userId, null, "USUARIO", null, null, null);
        Pageable pageable = PageRequest.of(0, 20);

        RegistroAuditoria registro = RegistroAuditoria.builder()
                .id(UUID.randomUUID())
                .eventId(UUID.randomUUID())
                .occurredOn(Instant.now())
                .eventType("auth.usuario.login")
                .userId(userId)
                .action("LOGIN")
                .resourceType("USUARIO")
                .resourceId(UUID.randomUUID())
                .sourceService("ms-auth")
                .receivedAt(Instant.now())
                .build();

        Page<RegistroAuditoria> page = new PageImpl<>(List.of(registro));
        when(auditoriaRepository.findByFiltro(filtro, pageable)).thenReturn(page);

        Page<RegistroAuditoria> result = auditoriaService.consultar(filtro, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(userId, result.getContent().getFirst().getUserId());
        verify(auditoriaRepository).findByFiltro(filtro, pageable);
    }

    @Test
    @DisplayName("Debe consultar sin filtros y retornar todos los registros")
    void debeConsultarSinFiltros() {
        FiltroAuditoria filtroVacio = new FiltroAuditoria(
                null, null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 20);

        Page<RegistroAuditoria> pageVacia = new PageImpl<>(List.of());
        when(auditoriaRepository.findByFiltro(filtroVacio, pageable)).thenReturn(pageVacia);

        Page<RegistroAuditoria> result = auditoriaService.consultar(filtroVacio, pageable);

        assertEquals(0, result.getTotalElements());
        verify(auditoriaRepository).findByFiltro(filtroVacio, pageable);
    }

    @Test
    @DisplayName("Debe mapear todos los campos del evento al registro")
    void debeMappearTodosLosCampos() {
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        AuditableEvent event = AuditableEvent.create(
                "historia.paciente.creado",
                userId,
                "obstetra@clinica.com",
                "CREATE",
                "PACIENTE",
                resourceId,
                null,
                "{\"nombre\":\"Ana\"}",
                "10.0.0.5",
                "ms-historia-clinica"
        );

        when(auditoriaRepository.save(any(RegistroAuditoria.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RegistroAuditoria result = auditoriaService.registrar(event);

        assertEquals(event.getEventId(), result.getEventId());
        assertEquals(event.getOccurredOn(), result.getOccurredOn());
        assertEquals("historia.paciente.creado", result.getEventType());
        assertEquals(userId, result.getUserId());
        assertEquals("obstetra@clinica.com", result.getUserEmail());
        assertEquals("CREATE", result.getAction());
        assertEquals("PACIENTE", result.getResourceType());
        assertEquals(resourceId, result.getResourceId());
        assertNull(result.getPreviousValue());
        assertEquals("{\"nombre\":\"Ana\"}", result.getNewValue());
        assertEquals("10.0.0.5", result.getSourceIp());
        assertEquals("ms-historia-clinica", result.getSourceService());
    }
}
