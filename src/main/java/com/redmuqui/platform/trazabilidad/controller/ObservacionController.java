package com.redmuqui.platform.trazabilidad.controller;

import com.redmuqui.platform.common.dto.PageResponse;
import com.redmuqui.platform.trazabilidad.dto.ObservacionRequestDTO;
import com.redmuqui.platform.trazabilidad.dto.ObservacionResponseDTO;
import com.redmuqui.platform.trazabilidad.service.ObservacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/observaciones")
@RequiredArgsConstructor
@Tag(name = "Observaciones", description = "Registro manual de observaciones e incidencias (HU022, RF-067, RF-068)")
public class ObservacionController {

    private final ObservacionService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'TECNICO', 'COORDINADOR')")
    @Operation(summary = "Registrar observación o incidencia (RF-067)")
    public ResponseEntity<ObservacionResponseDTO> crear(@Valid @RequestBody ObservacionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'TECNICO', 'COORDINADOR')")
    @Operation(summary = "Listar observaciones de una entidad de forma paginada (RF-068)")
    public ResponseEntity<PageResponse<ObservacionResponseDTO>> listarPorEntidad(
        @RequestParam String entidadReferenciada,
        @RequestParam Long idEntidadReferenciada,
        Pageable pageable
    ) {
        return ResponseEntity.ok(
            PageResponse.from(service.listarPorEntidad(entidadReferenciada, idEntidadReferenciada, pageable))
        );
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('DOCUMENTOS_VALIDATE')")
    public ResponseEntity<ObservacionResponseDTO> cambiarEstado(@PathVariable Long id, @RequestParam EstadoObservacion estado) {
        return ResponseEntity.ok(service.cambiarEstado(id, estado));
    }
}
