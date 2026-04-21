package com.redmuqui.platform.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI / Swagger UI.
 *
 * Acceder a la documentación en:
 *   - http://localhost:8080/swagger-ui.html
 *   - http://localhost:8080/api-docs
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("RedMuqui Platform API")
                .description("API REST para la Plataforma de Gestión de Proyectos, Informes y Trazabilidad Institucional de Red Muqui.")
                .version("0.1.0")
                .contact(new Contact()
                    .name("Equipo 1INF47 - PUCP")
                    .email("equipo@redmuqui.org"))
                .license(new License()
                    .name("Proyecto académico - PUCP 2026")))
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
            .components(new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME,
                    new SecurityScheme()
                        .name(SECURITY_SCHEME_NAME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
