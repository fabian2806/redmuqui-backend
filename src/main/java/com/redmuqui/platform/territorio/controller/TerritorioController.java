package com.redmuqui.platform.territorio.controller;

import com.redmuqui.platform.territorio.dto.TerritorioDTO;
import com.redmuqui.platform.territorio.service.TerritorioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/territorios")
@RequiredArgsConstructor
@Tag(name = "Territorios", description = "Catálogo de territorios")
public class TerritorioController {

    private final TerritorioService service;

    @GetMapping
    public ResponseEntity<List<TerritorioDTO>> listar() { return ResponseEntity.ok(service.listar()); }

    @GetMapping("/{id}")
    public ResponseEntity<TerritorioDTO> obtener(@PathVariable Long id) { return ResponseEntity.ok(service.obtener(id)); }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TerritorioDTO> crear(@Valid @RequestBody TerritorioDTO dto) {
        TerritorioDTO creado = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/v1/territorios/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<TerritorioDTO> actualizar(@PathVariable Long id, @Valid @RequestBody TerritorioDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
