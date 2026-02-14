package com.clinica.auditoria.domain.port.in;

import com.clinica.auditoria.domain.model.RegistroAuditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

public interface ConsultarAuditoriaUseCase {

    Page<RegistroAuditoria> consultar(FiltroAuditoria filtro, Pageable pageable);

    record FiltroAuditoria(
            UUID userId,
            UUID resourceId,
            String resourceType,
            String action,
            Instant desde,
            Instant hasta
    ) {
    }
}
