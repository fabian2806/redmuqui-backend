package com.redmuqui.platform.ia.controller;

import com.redmuqui.platform.ia.dto.ResumenIaResponse;
import com.redmuqui.platform.ia.service.ResumenIaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Resumen Ejecutivo con IA (Sprint 4 ⑤).
 *
 * Endpoint aislado del módulo de proyectos: comparte la ruta base
 * {@code /api/v1/proyectos} pero vive en su propio controller para no acoplar
 * la integración con IA al CRUD de proyectos.
 */
@RestController
@RequestMapping("/api/v1/proyectos")
@RequiredArgsConstructor
@Tag(name = "Resumen Ejecutivo con IA")
public class IaController {

    private final ResumenIaService service;

    @PostMapping("/{id}/resumen-ia")
    @PreAuthorize("hasAnyAuthority('PROYECTOS_READ', 'REPORTES_READ')")
    @Operation(summary = "Genera un resumen ejecutivo del proyecto con IA a partir de sus datos reales (Sprint 4 ⑤)")
    public ResponseEntity<ResumenIaResponse> generarResumen(@PathVariable Long id) {
        return ResponseEntity.ok(service.generarResumen(id));
    }
}
