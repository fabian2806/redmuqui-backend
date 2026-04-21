package com.redmuqui.platform.rol.controller;

import com.redmuqui.platform.rol.dto.PermisoDTO;
import com.redmuqui.platform.rol.dto.RolDTO;
import com.redmuqui.platform.rol.service.RolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Roles y Permisos")
public class RolController {

    private final RolService service;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar roles del sistema")
    public ResponseEntity<List<RolDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RolDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @GetMapping("/{id}/permisos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Permisos asociados a un rol (RF-017)")
    public ResponseEntity<List<PermisoDTO>> obtenerPermisos(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPermisos(id));
    }
}
