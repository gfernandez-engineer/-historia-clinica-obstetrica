package com.clinica.auth.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "ms-auth API",
                version = "1.0.0",
                description = "Microservicio de autenticacion y autorizacion - Registro, login, JWT, RBAC"
        )
)
public class AuthOpenApiConfig {
}
