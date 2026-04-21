package com.redmuqui.platform.institucion.controller;

import com.redmuqui.platform.institucion.dto.InstitucionDTO;
import com.redmuqui.platform.institucion.service.InstitucionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/instituciones")
@RequiredArgsConstructor
@Tag(name = "Instituciones", description = "Catálogo de instituciones miembro")
public class InstitucionController {

    private final InstitucionService service;

    @GetMapping
    public ResponseEntity<List<InstitucionDTO>> listar() { return ResponseEntity.ok(service.listar()); }

    @GetMapping("/{id}")
    public ResponseEntity<InstitucionDTO> obtener(@PathVariable Long id) { return ResponseEntity.ok(service.obtener(id)); }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<InstitucionDTO> crear(@Valid @RequestBody InstitucionDTO dto) {
        InstitucionDTO creado = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/v1/instituciones/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<InstitucionDTO> actualizar(@PathVariable Long id, @Valid @RequestBody InstitucionDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
