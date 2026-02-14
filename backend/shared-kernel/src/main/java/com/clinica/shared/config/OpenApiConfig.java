package com.clinica.shared.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Historia Clinica Obstetrica API",
                version = "1.0.0",
                description = "Sistema de historia clinica obstetrica por voz - API REST",
                contact = @Contact(name = "Equipo Clinica")
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token JWT de acceso. Obtener via POST /api/auth/login"
)
public class OpenApiConfig {
}
