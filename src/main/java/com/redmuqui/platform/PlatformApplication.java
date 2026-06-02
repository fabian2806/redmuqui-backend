package com.redmuqui.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada de la aplicación.
 *
 * Plataforma de Gestión de Proyectos, Informes y Trazabilidad Institucional
 * de Red Muqui.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class PlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformApplication.class, args);
    }
}
