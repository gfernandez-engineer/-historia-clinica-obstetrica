package com.clinica.exportacion.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExportacionOpenApiConfig {

    @Bean
    public OpenAPI exportacionOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Exportacion")
                        .description("Generacion y descarga de PDFs de historias clinicas obstetrica")
                        .version("1.0.0"));
    }
}
