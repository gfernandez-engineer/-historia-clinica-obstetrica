package com.clinica.transcripcion.infrastructure.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TranscripcionOpenApiConfig {

    @Bean
    public OpenAPI transcripcionOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Transcripcion Medica")
                        .description("Servicio de transcripcion y normalizacion de texto clinico obstetrico")
                        .version("1.0.0"));
    }
}
