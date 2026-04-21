package com.redmuqui.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Punto de entrada de la aplicación.
 *
 * Plataforma de Gestión de Proyectos, Informes y Trazabilidad Institucional
 * de Red Muqui.
 */
@SpringBootApplication
@EnableJpaAuditing
public class PlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformApplication.class, args);
    }
}
