package com.clinica.exportacion.domain.port.in;

import com.clinica.exportacion.domain.model.ExportJob;
import com.clinica.exportacion.domain.model.FormatoExportacion;

import java.util.UUID;

public interface GenerarExportacionUseCase {

    ExportJob generar(Command command);

    record Command(
            UUID historiaClinicaId,
            UUID obstetraId,
            FormatoExportacion formato,
            String jwtToken
    ) {
    }
}
