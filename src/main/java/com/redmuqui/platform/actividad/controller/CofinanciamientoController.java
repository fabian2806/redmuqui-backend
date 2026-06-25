package com.redmuqui.platform.actividad.controller;

import com.redmuqui.platform.actividad.dto.CofinanciamientoDisponibleDTO;
import com.redmuqui.platform.actividad.service.CofinanciamientoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cofinanciamiento")
@RequiredArgsConstructor
@Tag(name = "Cofinanciamiento")
public class CofinanciamientoController {

    private final CofinanciamientoService cofinanciamientoService;

    @GetMapping("/disponibles")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CofinanciamientoDisponibleDTO> listarDisponibles(
            @RequestParam String moneda,
            @RequestParam(required = false) Long excludeProyectoId) {
        return ResponseEntity.ok(cofinanciamientoService.listarDisponibles(moneda, excludeProyectoId));
    }
}
