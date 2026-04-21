package com.redmuqui.platform.trazabilidad.controller;

import com.redmuqui.platform.trazabilidad.dto.ObservacionCreateDTO;
import com.redmuqui.platform.trazabilidad.dto.ObservacionResponseDTO;
import com.redmuqui.platform.trazabilidad.entity.EstadoObservacion;
import com.redmuqui.platform.trazabilidad.service.ObservacionService;
import com.redmuqui.platform.usuario.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/observaciones")
@RequiredArgsConstructor
@Tag(name = "Observaciones")
public class ObservacionController {

    private final ObservacionService service;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<List<ObservacionResponseDTO>> listarPorEntidad(
        @RequestParam String entidad,
        @RequestParam Long idEntidad
    ) {
        return ResponseEntity.ok(service.listarPorEntidad(entidad, idEntidad));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'TECNICO', 'COORDINADOR')")
    public ResponseEntity<ObservacionResponseDTO> crear(
        @Valid @RequestBody ObservacionCreateDTO dto,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long idUsuario = usuarioRepository.findByEmailIgnoreCase(userDetails.getUsername())
            .orElseThrow().getId();
        return ResponseEntity.ok(service.crear(dto, idUsuario));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR')")
    public ResponseEntity<ObservacionResponseDTO> cambiarEstado(@PathVariable Long id, @RequestParam EstadoObservacion estado) {
        return ResponseEntity.ok(service.cambiarEstado(id, estado));
    }
}
