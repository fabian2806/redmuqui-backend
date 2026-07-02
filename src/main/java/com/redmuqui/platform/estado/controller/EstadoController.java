package com.redmuqui.platform.estado.controller;

import com.redmuqui.platform.estado.dto.EstadoCreateDTO;
import com.redmuqui.platform.estado.dto.EstadoResponseDTO;
import com.redmuqui.platform.estado.dto.EstadoUpdateDTO;
import com.redmuqui.platform.estado.entity.ModuloEstado;
import com.redmuqui.platform.estado.service.EstadoService;
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
@RequestMapping("/api/v1/estados")
@Tag(name = "Estados", description = "Catálogo de estados por módulo")
public class EstadoController {

    private final EstadoService service;

    public EstadoController(EstadoService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CATALOGOS_READ')")
    public ResponseEntity<List<EstadoResponseDTO>> listar(@RequestParam ModuloEstado modulo) {
        return ResponseEntity.ok(service.listar(modulo));
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('CATALOGOS_READ')")
    public ResponseEntity<Page<EstadoResponseDTO>> listarPaginado(@RequestParam ModuloEstado modulo, Pageable pageable) {
        return ResponseEntity.ok(service.listarPaginado(modulo, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CATALOGOS_READ')")
    public ResponseEntity<EstadoResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CATALOGOS_MANAGE')")
    public ResponseEntity<EstadoResponseDTO> crear(@Valid @RequestBody EstadoCreateDTO dto) {
        EstadoResponseDTO creado = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/v1/estados/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CATALOGOS_MANAGE')")
    public ResponseEntity<EstadoResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody EstadoUpdateDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CATALOGOS_MANAGE')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
