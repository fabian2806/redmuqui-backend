package com.redmuqui.platform.common.catalog.controller;

import com.redmuqui.platform.common.catalog.dto.BaseCatalogoDTO;
import com.redmuqui.platform.common.catalog.service.BaseCatalogoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;
import java.util.List;

public abstract class BaseCatalogoController<D extends BaseCatalogoDTO> {

    protected final BaseCatalogoService<?, D> service;

    protected BaseCatalogoController(BaseCatalogoService<?, D> service) {
        this.service = service;
    }

    protected abstract String getRutaBase();

    @GetMapping
    @PreAuthorize("hasAuthority('CATALOGOS_READ')")
    public ResponseEntity<List<D>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('CATALOGOS_READ')")
    public ResponseEntity<Page<D>> listarPaginado(Pageable pageable) {
        return ResponseEntity.ok(service.listarPaginado(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CATALOGOS_READ')")
    public ResponseEntity<D> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CATALOGOS_MANAGE')")
    public ResponseEntity<D> crear(@Valid @RequestBody D dto) {
        D creado = service.crear(dto);
        return ResponseEntity.created(URI.create(getRutaBase() + "/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CATALOGOS_MANAGE')")
    public ResponseEntity<D> actualizar(@PathVariable Long id, @Valid @RequestBody D dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CATALOGOS_MANAGE')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
