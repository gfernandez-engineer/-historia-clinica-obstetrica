package com.clinica.historiaclinica.domain.port.in;

import com.clinica.historiaclinica.domain.model.HistoriaClinica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ListarHistoriasClinicasUseCase {

    Page<HistoriaClinica> listarPorObstetra(UUID obstetraId, Pageable pageable);

    Page<HistoriaClinica> listarPorPaciente(UUID pacienteId, UUID obstetraId, Pageable pageable);
}
