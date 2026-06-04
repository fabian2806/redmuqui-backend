package com.redmuqui.platform.trazabilidad.controller;

import com.redmuqui.platform.common.dto.PageResponse;
import com.redmuqui.platform.trazabilidad.dto.BitacoraConsultaDTO;
import com.redmuqui.platform.trazabilidad.service.BitacoraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bitacora")
@RequiredArgsConstructor
@Tag(name = "Bitácora", description = "Consulta de bitácora y seguimiento histórico (HU021, RF-064 a RF-066)")
public class BitacoraController {

    private final BitacoraService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR')")
    @Operation(summary = "Listar eventos de auditoría paginados (RF-064)")
    public ResponseEntity<PageResponse<BitacoraConsultaDTO>> consultarGeneral(Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(service.consultarGeneral(pageable)));
    }

    @GetMapping("/entidades/{entidadReferenciada}/{idEntidadRef}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR')")
    @Operation(summary = "Historial de una entidad referenciada paginado (RF-065)")
    public ResponseEntity<PageResponse<BitacoraConsultaDTO>> consultarHistorialEntidad(
        @PathVariable String entidadReferenciada,
        @PathVariable Long idEntidadRef,
        Pageable pageable
    ) {
        return ResponseEntity.ok(
            PageResponse.from(service.consultarHistorialEntidad(entidadReferenciada, idEntidadRef, pageable))
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR')")
    public ResponseEntity<PageResponse<BitacoraResponseDTO>> listar(
        @RequestParam(required = false) String entidad,
        @RequestParam(required = false) Long idEntidad,
        Pageable pageable
    ) {
        return ResponseEntity.ok(
            PageResponse.from(service.consultarHistorialEntidad(entidadReferenciada, idEntidadRef, pageable))
        );
    }
}
