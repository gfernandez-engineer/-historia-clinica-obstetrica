package com.clinica.auditoria.domain.port.in;

import com.clinica.auditoria.domain.model.RegistroAuditoria;
import com.clinica.shared.domain.event.AuditableEvent;

public interface RegistrarAuditoriaUseCase {

    RegistroAuditoria registrar(AuditableEvent event);
}
