package com.redmuqui.platform.tipodocumento.controller;

import com.redmuqui.platform.tipodocumento.dto.TipoDocumentoCreateDTO;
import com.redmuqui.platform.tipodocumento.dto.TipoDocumentoResponseDTO;
import com.redmuqui.platform.tipodocumento.dto.TipoDocumentoUpdateDTO;
import com.redmuqui.platform.tipodocumento.service.TipoDocumentoService;
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
@RequestMapping("/api/v1/tipos-documento")
@Tag(name = "Tipos de documento", description = "Catálogo de tipos de documento")
public class TipoDocumentoController {

    private final TipoDocumentoService service;

    public TipoDocumentoController(TipoDocumentoService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CATALOGOS_READ')")
    public ResponseEntity<List<TipoDocumentoResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('CATALOGOS_READ')")
    public ResponseEntity<Page<TipoDocumentoResponseDTO>> listarPaginado(Pageable pageable) {
        return ResponseEntity.ok(service.listarPaginado(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CATALOGOS_READ')")
    public ResponseEntity<TipoDocumentoResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CATALOGOS_MANAGE')")
    public ResponseEntity<TipoDocumentoResponseDTO> crear(@Valid @RequestBody TipoDocumentoCreateDTO dto) {
        TipoDocumentoResponseDTO creado = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/v1/tipos-documento/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CATALOGOS_MANAGE')")
    public ResponseEntity<TipoDocumentoResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody TipoDocumentoUpdateDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CATALOGOS_MANAGE')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
