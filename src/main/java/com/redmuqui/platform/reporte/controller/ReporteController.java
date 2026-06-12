package com.redmuqui.platform.reporte.controller;

import com.redmuqui.platform.reporte.dto.ConteoDTO;
import com.redmuqui.platform.reporte.dto.DocumentoRecienteDTO;
import com.redmuqui.platform.reporte.dto.IndicadoresDTO;
import com.redmuqui.platform.reporte.dto.ProyectoRiesgoDTO;
import com.redmuqui.platform.reporte.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Módulo de Reportes e Indicadores (RF-069 a RF-074).
 * Agregaciones en servidor para el dashboard ejecutivo.
 */
@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes e Indicadores")
public class ReporteController {

    private final ReporteService service;

    @GetMapping("/indicadores")
    @PreAuthorize("hasAuthority('REPORTES_READ')")
    @Operation(summary = "Indicadores globales de la red para el dashboard (RF-069)")
    public ResponseEntity<IndicadoresDTO> indicadores() {
        return ResponseEntity.ok(service.obtenerIndicadores());
    }

    @GetMapping("/proyectos-por-macroregion")
    @PreAuthorize("hasAuthority('REPORTES_READ')")
    @Operation(summary = "Conteo de proyectos por macroregión (RF-073)")
    public ResponseEntity<List<ConteoDTO>> proyectosPorMacroregion() {
        return ResponseEntity.ok(service.proyectosPorMacroregion());
    }

    @GetMapping("/actividades-por-estado")
    @PreAuthorize("hasAuthority('REPORTES_READ')")
    @Operation(summary = "Distribución de actividades por estado, con vencidas derivadas (RF-074)")
    public ResponseEntity<List<ConteoDTO>> actividadesPorEstado() {
        return ResponseEntity.ok(service.actividadesPorEstado());
    }

    @GetMapping("/proyectos-en-riesgo")
    @PreAuthorize("hasAuthority('REPORTES_READ')")
    @Operation(summary = "Proyectos activos clasificados en riesgo (RF-071)")
    public ResponseEntity<List<ProyectoRiesgoDTO>> proyectosEnRiesgo() {
        return ResponseEntity.ok(service.proyectosEnRiesgo());
    }

    @GetMapping("/documentos-recientes")
    @PreAuthorize("hasAuthority('REPORTES_READ')")
    @Operation(summary = "Últimos documentos registrados (RF-072)")
    public ResponseEntity<List<DocumentoRecienteDTO>> documentosRecientes() {
        return ResponseEntity.ok(service.documentosRecientes());
    }
}
