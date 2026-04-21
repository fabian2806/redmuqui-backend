package com.redmuqui.platform.trazabilidad.controller;

import com.redmuqui.platform.common.dto.PageResponse;
import com.redmuqui.platform.trazabilidad.dto.BitacoraResponseDTO;
import com.redmuqui.platform.trazabilidad.service.BitacoraService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bitacora")
@RequiredArgsConstructor
@Tag(name = "Bitácora")
public class BitacoraController {

    private final BitacoraService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'COORDINADOR')")
    public ResponseEntity<PageResponse<BitacoraResponseDTO>> listar(
        @RequestParam(required = false) String entidad,
        @RequestParam(required = false) Long idEntidad,
        Pageable pageable
    ) {
        if (entidad != null && idEntidad != null) {
            return ResponseEntity.ok(PageResponse.from(service.listarPorEntidad(entidad, idEntidad, pageable)));
        }
        return ResponseEntity.ok(PageResponse.from(service.listar(pageable)));
    }
}
