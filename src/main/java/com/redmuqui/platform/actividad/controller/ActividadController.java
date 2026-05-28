package com.redmuqui.platform.actividad.controller;

import com.redmuqui.platform.actividad.dto.ActividadCreateDTO;
import com.redmuqui.platform.actividad.dto.ActividadResponseDTO;
import com.redmuqui.platform.actividad.dto.ActividadUpdateDTO;
import com.redmuqui.platform.actividad.entity.EstadoActividad;
import com.redmuqui.platform.actividad.service.ActividadService;
import com.redmuqui.platform.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/actividades")
@RequiredArgsConstructor
@Tag(name = "Actividades")
public class ActividadController {

    private final ActividadService service;

    @GetMapping
    public ResponseEntity<PageResponse<ActividadResponseDTO>> listar(
            @RequestParam(required = false) Long proyectoId,
            Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(service.listar(proyectoId, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActividadResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'TECNICO')")
    public ResponseEntity<ActividadResponseDTO> crear(@Valid @RequestBody ActividadCreateDTO dto) {
        ActividadResponseDTO creado = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/v1/actividades/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'TECNICO')")
    public ResponseEntity<ActividadResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ActividadUpdateDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'TECNICO')")
    public ResponseEntity<ActividadResponseDTO> cambiarEstado(@PathVariable Long id, @RequestParam EstadoActividad estado) {
        return ResponseEntity.ok(service.cambiarEstado(id, estado));
    }

    @PatchMapping("/{id}/avance")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'TECNICO')")
    public ResponseEntity<ActividadResponseDTO> actualizarAvance(@PathVariable Long id, @RequestParam Integer porcentajeAvance) {
        return ResponseEntity.ok(service.actualizarAvance(id, porcentajeAvance));
    }
}
