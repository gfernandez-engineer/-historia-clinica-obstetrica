package com.clinica.exportacion.domain.port.in;

import java.util.UUID;

public interface DescargarPdfUseCase {

    byte[] descargar(UUID exportJobId, UUID obstetraId, String jwtToken);
}
