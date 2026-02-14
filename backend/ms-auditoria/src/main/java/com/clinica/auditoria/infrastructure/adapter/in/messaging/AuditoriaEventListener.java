package com.clinica.auditoria.infrastructure.adapter.in.messaging;

import com.clinica.auditoria.domain.port.in.RegistrarAuditoriaUseCase;
import com.clinica.shared.domain.event.AuditableEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditoriaEventListener {

    private final RegistrarAuditoriaUseCase registrarAuditoriaUseCase;

    @RabbitListener(queues = "${auditoria.queue.name:clinica.auditoria.queue}")
    public void onAuditableEvent(AuditableEvent event) {
        try {
            log.debug("Evento recibido: {} de {}", event.getEventType(), event.getSourceService());
            registrarAuditoriaUseCase.registrar(event);
        } catch (Exception e) {
            log.error("Error procesando evento de auditoria: {} - {}", event.getEventType(), e.getMessage(), e);
        }
    }
}
