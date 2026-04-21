package com.redmuqui.platform.actividad.controller;

import com.redmuqui.platform.actividad.dto.HitoCreateDTO;
import com.redmuqui.platform.actividad.dto.HitoResponseDTO;
import com.redmuqui.platform.actividad.entity.EstadoHito;
import com.redmuqui.platform.actividad.service.HitoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/hitos")
@RequiredArgsConstructor
@Tag(name = "Hitos")
public class HitoController {

    private final HitoService service;

    @GetMapping
    public ResponseEntity<List<HitoResponseDTO>> listarPorProyecto(@RequestParam Long idProyecto) {
        return ResponseEntity.ok(service.listarPorProyecto(idProyecto));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'TECNICO')")
    public ResponseEntity<HitoResponseDTO> crear(@Valid @RequestBody HitoCreateDTO dto) {
        HitoResponseDTO creado = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/v1/hitos/" + creado.id())).body(creado);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'TECNICO')")
    public ResponseEntity<HitoResponseDTO> cambiarEstado(@PathVariable Long id, @RequestParam EstadoHito estado) {
        return ResponseEntity.ok(service.cambiarEstado(id, estado));
    }
}
