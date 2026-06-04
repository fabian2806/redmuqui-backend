package com.redmuqui.platform.reporte.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Esqueleto del módulo de Reportes e Indicadores (RFs 069+).
 * La implementación detallada se completará una vez confirmados los reportes
 * exactos que el cliente requiere.
 */
@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes e Indicadores")
public class ReporteController {

    @GetMapping("/proyectos")
    @PreAuthorize("hasAuthority('REPORTES_READ')")
    public ResponseEntity<Map<String, String>> reporteProyectos() {
        return ResponseEntity.ok(Map.of("status", "pendiente de implementación"));
    }

    @GetMapping("/actividad-reciente")
    @PreAuthorize("hasAuthority('REPORTES_READ')")
    public ResponseEntity<Map<String, String>> actividadReciente() {
        return ResponseEntity.ok(Map.of("status", "pendiente de implementación"));
    }
}
