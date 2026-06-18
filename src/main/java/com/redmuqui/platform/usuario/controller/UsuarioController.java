package com.redmuqui.platform.usuario.controller;

import com.redmuqui.platform.common.dto.PageResponse;
import com.redmuqui.platform.usuario.dto.UsuarioCreateDTO;
import com.redmuqui.platform.usuario.dto.UsuarioPerfilUpdateDTO;
import com.redmuqui.platform.usuario.dto.UsuarioPerfilUpdateResponseDTO;
import com.redmuqui.platform.usuario.dto.UsuarioResponseDTO;
import com.redmuqui.platform.usuario.dto.UsuarioUpdateDTO;
import com.redmuqui.platform.usuario.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema (RF-001 a RF-018)")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasAuthority('USUARIOS_READ')")
    @Operation(summary = "Listar usuarios paginados (RF-015)")
    public ResponseEntity<PageResponse<UsuarioResponseDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(usuarioService.listar(pageable)));
    }

    @GetMapping("/me")
    @Operation(summary = "Información del usuario autenticado (RF-006)")
    public ResponseEntity<UsuarioResponseDTO> obtenerPropio(Authentication authentication) {
        return ResponseEntity.ok(usuarioService.obtenerPorEmail(obtenerEmailAutenticado(authentication)));
    }

    @PutMapping("/me")
    @Operation(summary = "Actualizar datos personales del usuario autenticado")
    public ResponseEntity<UsuarioPerfilUpdateResponseDTO> actualizarPropio(
        Authentication authentication,
        @Valid @RequestBody UsuarioPerfilUpdateDTO dto
    ) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(obtenerEmailAutenticado(authentication), dto));
    }

    private String obtenerEmailAutenticado(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }

        String email = authentication.getName();

        if (email == null || email.equals("anonymousUser")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }

        return email;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIOS_READ')")
    @Operation(summary = "Obtener detalle de un usuario")
    public ResponseEntity<UsuarioResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USUARIOS_CREATE')")
    @Operation(summary = "Registrar un nuevo usuario (RF-012)")
    public ResponseEntity<UsuarioResponseDTO> crear(@Valid @RequestBody UsuarioCreateDTO dto) {
        UsuarioResponseDTO creado = usuarioService.crear(dto);
        return ResponseEntity.created(URI.create("/api/v1/usuarios/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIOS_UPDATE')")
    @Operation(summary = "Actualizar información de un usuario (RF-013)")
    public ResponseEntity<UsuarioResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizar(id, dto));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('USUARIOS_DEACTIVATE')")
    @Operation(summary = "Activar o desactivar un usuario (RF-014)")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        usuarioService.cambiarEstado(id, activo);
        return ResponseEntity.noContent().build();
    }
}
