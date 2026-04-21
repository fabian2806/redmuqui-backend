package com.redmuqui.platform.ejetematico.controller;

import com.redmuqui.platform.ejetematico.dto.EjeTematicoDTO;
import com.redmuqui.platform.ejetematico.service.EjeTematicoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ejes-tematicos")
@RequiredArgsConstructor
@Tag(name = "Ejes Temáticos", description = "Catálogo de ejes temáticos")
public class EjeTematicoController {

    private final EjeTematicoService service;

    @GetMapping
    public ResponseEntity<List<EjeTematicoDTO>> listar() { return ResponseEntity.ok(service.listar()); }

    @GetMapping("/{id}")
    public ResponseEntity<EjeTematicoDTO> obtener(@PathVariable Long id) { return ResponseEntity.ok(service.obtener(id)); }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<EjeTematicoDTO> crear(@Valid @RequestBody EjeTematicoDTO dto) {
        EjeTematicoDTO creado = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/v1/ejes-tematicos/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<EjeTematicoDTO> actualizar(@PathVariable Long id, @Valid @RequestBody EjeTematicoDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
