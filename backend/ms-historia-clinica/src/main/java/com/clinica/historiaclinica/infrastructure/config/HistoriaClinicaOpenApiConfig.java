package com.clinica.historiaclinica.infrastructure.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HistoriaClinicaOpenApiConfig {

    @Bean
    public OpenAPI historiaClinicaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Historia Clinica Obstetrica")
                        .description("Gestion de pacientes e historias clinicas obstétricas")
                        .version("1.0.0"));
    }
}
