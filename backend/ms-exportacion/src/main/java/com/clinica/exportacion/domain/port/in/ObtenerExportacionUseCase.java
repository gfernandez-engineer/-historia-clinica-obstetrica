package com.clinica.exportacion.domain.port.in;

import com.clinica.exportacion.domain.model.ExportJob;

import java.util.UUID;

public interface ObtenerExportacionUseCase {

    ExportJob obtener(UUID exportJobId, UUID obstetraId);
}
