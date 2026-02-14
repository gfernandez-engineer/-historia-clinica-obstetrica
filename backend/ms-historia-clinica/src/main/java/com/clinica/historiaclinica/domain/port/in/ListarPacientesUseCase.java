package com.clinica.historiaclinica.domain.port.in;

import com.clinica.historiaclinica.domain.model.Paciente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ListarPacientesUseCase {

    Page<Paciente> listarPorObstetra(UUID obstetraId, Pageable pageable);

    Page<Paciente> buscarPorNombreODni(UUID obstetraId, String termino, Pageable pageable);
}
