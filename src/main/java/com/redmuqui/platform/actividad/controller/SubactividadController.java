package com.redmuqui.platform.actividad.controller;

import com.redmuqui.platform.actividad.dto.SubactividadCofinanciamientoCreateDTO;
import com.redmuqui.platform.actividad.dto.SubactividadCreateDTO;
import com.redmuqui.platform.actividad.dto.SubactividadResponseDTO;
import com.redmuqui.platform.actividad.service.SubactividadService;
import com.redmuqui.platform.actividad.entity.EstadoEvidencia;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/actividades/{actividadId}/subactividades")
@RequiredArgsConstructor
public class SubactividadController {

    private final SubactividadService subactividadService;

    @PostMapping
    @PreAuthorize("hasAuthority('PROYECTOS_UPDATE')")
    public ResponseEntity<SubactividadResponseDTO> crear(
            @PathVariable Long actividadId,
            @Valid @RequestBody SubactividadCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subactividadService.crear(actividadId, dto));
    }

    @PutMapping("/{subactividadId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'TECNICO')")
    public ResponseEntity<SubactividadResponseDTO> actualizar(
            @PathVariable Long actividadId,
            @PathVariable Long subactividadId,
            @Valid @RequestBody SubactividadCreateDTO dto) {
        return ResponseEntity.ok(subactividadService.actualizar(subactividadId, dto));
    }

    @DeleteMapping("/{subactividadId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'TECNICO')")
    public ResponseEntity<Void> eliminar(@PathVariable Long subactividadId) {
        subactividadService.eliminar(subactividadId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{subactividadId}/cofinanciamientos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<SubactividadResponseDTO> cofinanciar(
            @PathVariable Long subactividadId,
            @Valid @RequestBody SubactividadCofinanciamientoCreateDTO dto) {
        return ResponseEntity.ok(subactividadService.cofinanciar(subactividadId, dto));
    }

    @DeleteMapping("/{subactividadId}/cofinanciamientos/{actividadOrigenId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarCofinanciamiento(
            @PathVariable Long subactividadId,
            @PathVariable Long actividadOrigenId) {
        subactividadService.eliminarCofinanciamiento(subactividadId, actividadOrigenId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{subactividadId}/evidencias")
    @PreAuthorize("hasAuthority('PROYECTOS_UPDATE')")
    public ResponseEntity<SubactividadResponseDTO> subirEvidencia(
            @PathVariable Long subactividadId,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) Integer hombresInvolucrados,
            @RequestParam(required = false) Integer mujeresInvolucradas) {
        return ResponseEntity.ok(subactividadService.subirEvidencia(subactividadId, file, hombresInvolucrados, mujeresInvolucradas));
    }

    @PatchMapping("/{subactividadId}/evidencias/{archivoId}/estado")
    @PreAuthorize("hasAuthority('PROYECTOS_UPDATE')")
    public ResponseEntity<SubactividadResponseDTO> cambiarEstadoEvidencia(
            @PathVariable Long subactividadId,
            @PathVariable Long archivoId,
            @RequestParam EstadoEvidencia estado) {
        return ResponseEntity.ok(
            subactividadService.cambiarEstadoEvidencia(subactividadId, archivoId, estado)
        );
    }
}
