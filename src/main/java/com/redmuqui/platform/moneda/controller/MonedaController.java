package com.redmuqui.platform.moneda.controller;

import com.redmuqui.platform.moneda.dto.MonedaCreateDTO;
import com.redmuqui.platform.moneda.dto.MonedaResponseDTO;
import com.redmuqui.platform.moneda.dto.MonedaUpdateDTO;
import com.redmuqui.platform.moneda.service.MonedaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/monedas")
@Tag(name = "Monedas", description = "Catálogo de monedas")
public class MonedaController {

    private final MonedaService service;

    public MonedaController(MonedaService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CATALOGOS_READ')")
    public ResponseEntity<List<MonedaResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('CATALOGOS_READ')")
    public ResponseEntity<Page<MonedaResponseDTO>> listarPaginado(Pageable pageable) {
        return ResponseEntity.ok(service.listarPaginado(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CATALOGOS_READ')")
    public ResponseEntity<MonedaResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CATALOGOS_MANAGE')")
    public ResponseEntity<MonedaResponseDTO> crear(@Valid @RequestBody MonedaCreateDTO dto) {
        MonedaResponseDTO creado = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/v1/monedas/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CATALOGOS_MANAGE')")
    public ResponseEntity<MonedaResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody MonedaUpdateDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CATALOGOS_MANAGE')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
