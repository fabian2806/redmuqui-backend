package com.redmuqui.platform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de CORS.
 *
 * OWASP A05 – Security Misconfiguration:
 *   ANTES: setAllowedHeaders(List.of("*")) → aceptaba cualquier header del cliente,
 *   lo que puede filtrar headers internos o permitir ataques de CORS avanzados.
 *
 *   AHORA: se declaran explícitamente solo los headers que el frontend necesita.
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));

        // OWASP A05: declarar explícitamente los headers permitidos, no usar "*".
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Cache-Control"
        ));

        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // Solo exponer el header Authorization al frontend.
        config.setExposedHeaders(List.of("Authorization"));

        // Pre-flight cacheado 30 min para reducir OPTIONS requests.
        config.setMaxAge(1800L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}