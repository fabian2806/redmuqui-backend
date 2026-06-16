package com.redmuqui.platform.reporte.controller;

import com.redmuqui.platform.reporte.dto.ActividadRecienteDTO;
import com.redmuqui.platform.reporte.dto.CoberturaTerritorialDTO;
import com.redmuqui.platform.reporte.dto.ConteoDTO;
import com.redmuqui.platform.reporte.dto.ConteoPresupuestoDTO;
import com.redmuqui.platform.reporte.dto.DocumentoRecienteDTO;
import com.redmuqui.platform.reporte.dto.IndicadoresDTO;
import com.redmuqui.platform.reporte.dto.MacroregionResumenDTO;
import com.redmuqui.platform.reporte.dto.ProyectoAvanceDTO;
import com.redmuqui.platform.reporte.dto.ProyectoRiesgoDTO;
import com.redmuqui.platform.reporte.service.ReporteService;
import com.redmuqui.platform.territorio.entity.TipoTerritorio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/proyectos-por-estado")
    @PreAuthorize("hasAuthority('REPORTES_READ')")
    @Operation(summary = "Conteo de proyectos por estado")
    public ResponseEntity<List<ConteoDTO>> proyectosPorEstado() {
        return ResponseEntity.ok(service.proyectosPorEstado());
    }

    @GetMapping("/proyectos-por-eje")
    @PreAuthorize("hasAuthority('REPORTES_READ')")
    @Operation(summary = "Conteo y presupuesto de proyectos por eje tematico")
    public ResponseEntity<List<ConteoPresupuestoDTO>> proyectosPorEjeTematico() {
        return ResponseEntity.ok(service.proyectosPorEjeTematico());
    }

    @GetMapping("/avance-proyectos")
    @PreAuthorize("hasAuthority('REPORTES_READ')")
    @Operation(summary = "Resumen de avance fisico por proyecto")
    public ResponseEntity<List<ProyectoAvanceDTO>> avanceProyectos() {
        return ResponseEntity.ok(service.avanceProyectos());
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

    @GetMapping("/documentos-por-tipo")
    @PreAuthorize("hasAuthority('REPORTES_READ')")
    @Operation(summary = "Conteo de documentos por tipo")
    public ResponseEntity<List<ConteoDTO>> documentosPorTipo() {
        return ResponseEntity.ok(service.documentosPorTipo());
    }

    @GetMapping("/documentos-por-estado")
    @PreAuthorize("hasAuthority('REPORTES_READ')")
    @Operation(summary = "Conteo de documentos por estado")
    public ResponseEntity<List<ConteoDTO>> documentosPorEstado() {
        return ResponseEntity.ok(service.documentosPorEstado());
    }

    @GetMapping("/resumen-macroregiones")
    @PreAuthorize("hasAuthority('REPORTES_READ')")
    @Operation(summary = "Resumen geografico por macroregion")
    public ResponseEntity<List<MacroregionResumenDTO>> resumenMacroregiones() {
        return ResponseEntity.ok(service.resumenMacroregiones());
    }

    @GetMapping("/actividad-reciente")
    @PreAuthorize("hasAuthority('REPORTES_READ')")
    @Operation(summary = "Eventos recientes para el panel de reportes")
    public ResponseEntity<List<ActividadRecienteDTO>> actividadReciente() {
        return ResponseEntity.ok(service.actividadReciente());
    }

    @GetMapping("/cobertura-territorial")
    @PreAuthorize("hasAuthority('REPORTES_READ')")
    @Operation(summary = "Cobertura de la red por territorio para el mapa, por nivel (Sprint 4 ④)")
    public ResponseEntity<List<CoberturaTerritorialDTO>> coberturaTerritorial(
            @RequestParam(name = "nivel", defaultValue = "DEPARTAMENTO") TipoTerritorio nivel) {
        return ResponseEntity.ok(service.coberturaTerritorial(nivel));
    }
}
