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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Hitos")
public class HitoController {

    private final HitoService service;

    @GetMapping("/proyectos/{idProyecto}/hitos")
    @PreAuthorize("hasAuthority('PROYECTOS_READ')")
    public ResponseEntity<List<HitoResponseDTO>> listarPorProyectoPath(@PathVariable Long idProyecto) {
        return ResponseEntity.ok(service.listarPorProyecto(idProyecto));
    }

    @PostMapping("/proyectos/{idProyecto}/hitos")
    @PreAuthorize("hasAuthority('PROYECTOS_UPDATE')")
    public ResponseEntity<HitoResponseDTO> crearEnProyecto(
        @PathVariable Long idProyecto,
        @Valid @RequestBody HitoCreateDTO dto
    ) {
        HitoResponseDTO creado = service.crear(idProyecto, dto);
        return ResponseEntity.created(URI.create("/api/v1/proyectos/" + idProyecto + "/hitos/" + creado.id())).body(creado);
    }

    @PutMapping("/proyectos/{idProyecto}/hitos/{id}")
    @PreAuthorize("hasAuthority('PROYECTOS_UPDATE')")
    public ResponseEntity<HitoResponseDTO> actualizar(
        @PathVariable Long idProyecto,
        @PathVariable Long id,
        @Valid @RequestBody HitoCreateDTO dto
    ) {
        return ResponseEntity.ok(service.actualizar(idProyecto, id, dto));
    }

    @DeleteMapping("/proyectos/{idProyecto}/hitos/{id}")
    @PreAuthorize("hasAuthority('PROYECTOS_UPDATE')")
    public ResponseEntity<Void> eliminar(@PathVariable Long idProyecto, @PathVariable Long id) {
        service.eliminar(idProyecto, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/hitos")
    @PreAuthorize("hasAuthority('PROYECTOS_READ')")
    public ResponseEntity<List<HitoResponseDTO>> listarPorProyecto(@RequestParam Long idProyecto) {
        return ResponseEntity.ok(service.listarPorProyecto(idProyecto));
    }

    @PostMapping("/hitos")
    @PreAuthorize("hasAuthority('PROYECTOS_UPDATE')")
    public ResponseEntity<HitoResponseDTO> crear(@Valid @RequestBody HitoCreateDTO dto) {
        HitoResponseDTO creado = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/v1/hitos/" + creado.id())).body(creado);
    }

    @PatchMapping("/hitos/{id}/estado")
    @PreAuthorize("hasAuthority('PROYECTOS_UPDATE')")
    public ResponseEntity<HitoResponseDTO> cambiarEstado(@PathVariable Long id, @RequestParam EstadoHito estado) {
        return ResponseEntity.ok(service.cambiarEstado(id, estado));
    }
}
