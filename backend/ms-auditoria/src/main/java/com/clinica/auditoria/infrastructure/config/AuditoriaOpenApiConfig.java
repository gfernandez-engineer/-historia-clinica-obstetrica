package com.clinica.auditoria.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditoriaOpenApiConfig {

    @Bean
    public OpenAPI auditoriaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Auditoria")
                        .description("Consulta de registros de auditoria del sistema")
                        .version("1.0.0"));
    }
}
