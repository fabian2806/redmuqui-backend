package com.redmuqui.platform.actividad.controller;

import com.redmuqui.platform.actividad.dto.FaseCreateDTO;
import com.redmuqui.platform.actividad.dto.FaseResponseDTO;
import com.redmuqui.platform.actividad.service.FaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/proyectos/{proyectoId}/fases")
@RequiredArgsConstructor
public class FaseController {

    private final FaseService faseService;

    @GetMapping
    @PreAuthorize("hasAuthority('PROYECTOS_READ')")
    public ResponseEntity<List<FaseResponseDTO>> listar(@PathVariable Long proyectoId) {
        return ResponseEntity.ok(faseService.listarPorProyecto(proyectoId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROYECTOS_UPDATE')")
    public ResponseEntity<FaseResponseDTO> crear(
        @PathVariable Long proyectoId,
        @Valid @RequestBody FaseCreateDTO dto
    ) {
        FaseResponseDTO creada = faseService.crear(proyectoId, dto);
        return ResponseEntity
            .created(URI.create("/api/v1/proyectos/" + proyectoId + "/fases/" + creada.id()))
            .body(creada);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROYECTOS_UPDATE')")
    public ResponseEntity<FaseResponseDTO> actualizar(
        @PathVariable Long proyectoId,
        @PathVariable Long id,
        @Valid @RequestBody FaseCreateDTO dto
    ) {
        return ResponseEntity.ok(faseService.actualizar(proyectoId, id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROYECTOS_UPDATE')")
    public ResponseEntity<Void> eliminar(@PathVariable Long proyectoId, @PathVariable Long id) {
        faseService.eliminar(proyectoId, id);
        return ResponseEntity.noContent().build();
    }
}
