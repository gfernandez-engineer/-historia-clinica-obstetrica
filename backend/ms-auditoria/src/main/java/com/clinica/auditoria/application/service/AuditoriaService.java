package com.clinica.auditoria.application.service;

import com.clinica.auditoria.domain.model.RegistroAuditoria;
import com.clinica.auditoria.domain.port.in.ConsultarAuditoriaUseCase;
import com.clinica.auditoria.domain.port.in.RegistrarAuditoriaUseCase;
import com.clinica.auditoria.domain.port.out.AuditoriaRepositoryPort;
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
public class AuditoriaService implements RegistrarAuditoriaUseCase, ConsultarAuditoriaUseCase {

    private final AuditoriaRepositoryPort auditoriaRepository;

    @Override
    @Transactional
    public RegistroAuditoria registrar(AuditableEvent event) {
        RegistroAuditoria registro = RegistroAuditoria.builder()
                .id(UUID.randomUUID())
                .eventId(event.getEventId())
                .occurredOn(event.getOccurredOn())
                .eventType(event.getEventType())
                .userId(event.getUserId())
                .userEmail(event.getUserEmail())
                .action(event.getAction())
                .resourceType(event.getResourceType())
                .resourceId(event.getResourceId())
                .previousValue(event.getPreviousValue())
                .newValue(event.getNewValue())
                .sourceIp(event.getSourceIp())
                .sourceService(event.getSourceService())
                .receivedAt(Instant.now())
                .build();

        RegistroAuditoria saved = auditoriaRepository.save(registro);
        log.info("Auditoria registrada: {} - {} - {}", saved.getAction(), saved.getResourceType(), saved.getResourceId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RegistroAuditoria> consultar(FiltroAuditoria filtro, Pageable pageable) {
        return auditoriaRepository.findByFiltro(filtro, pageable);
    }
}
