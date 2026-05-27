package com.redmuqui.platform.actividad.controller;

import com.redmuqui.platform.actividad.dto.SubactividadCofinanciamientoCreateDTO;
import com.redmuqui.platform.actividad.dto.SubactividadCreateDTO;
import com.redmuqui.platform.actividad.dto.SubactividadResponseDTO;
import com.redmuqui.platform.actividad.service.SubactividadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/actividades/{actividadId}/subactividades")
@RequiredArgsConstructor
public class SubactividadController {

    private final SubactividadService subactividadService;

    @PostMapping
    public ResponseEntity<SubactividadResponseDTO> crear(
            @PathVariable Long actividadId,
            @Valid @RequestBody SubactividadCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subactividadService.crear(actividadId, dto));
    }

    @PostMapping("/{subactividadId}/cofinanciamientos")
    public ResponseEntity<SubactividadResponseDTO> cofinanciar(
            @PathVariable Long subactividadId,
            @Valid @RequestBody SubactividadCofinanciamientoCreateDTO dto) {
        return ResponseEntity.ok(subactividadService.cofinanciar(subactividadId, dto));
    }

    @PostMapping("/{subactividadId}/evidencias")
    public ResponseEntity<SubactividadResponseDTO> subirEvidencia(
            @PathVariable Long subactividadId,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) Integer hombresInvolucrados,
            @RequestParam(required = false) Integer mujeresInvolucradas) {
        return ResponseEntity.ok(subactividadService.subirEvidencia(subactividadId, file, hombresInvolucrados, mujeresInvolucradas));
    }
}
