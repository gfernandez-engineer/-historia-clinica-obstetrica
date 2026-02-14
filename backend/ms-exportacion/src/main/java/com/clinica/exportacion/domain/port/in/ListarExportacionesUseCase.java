package com.clinica.exportacion.domain.port.in;

import com.clinica.exportacion.domain.model.ExportJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ListarExportacionesUseCase {

    Page<ExportJob> listarPorObstetra(UUID obstetraId, Pageable pageable);
}
