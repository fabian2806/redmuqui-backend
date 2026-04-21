package com.redmuqui.platform.macroregion.controller;

import com.redmuqui.platform.macroregion.dto.MacroregionDTO;
import com.redmuqui.platform.macroregion.service.MacroregionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/macroregiones")
@RequiredArgsConstructor
@Tag(name = "Macroregiones", description = "Catálogo de macroregiones")
public class MacroregionController {

    private final MacroregionService service;

    @GetMapping
    public ResponseEntity<List<MacroregionDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MacroregionDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MacroregionDTO> crear(@Valid @RequestBody MacroregionDTO dto) {
        MacroregionDTO creado = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/v1/macroregiones/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MacroregionDTO> actualizar(@PathVariable Long id, @Valid @RequestBody MacroregionDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
