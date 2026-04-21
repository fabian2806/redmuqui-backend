package com.redmuqui.platform.proyecto.controller;

import com.redmuqui.platform.common.dto.PageResponse;
import com.redmuqui.platform.proyecto.dto.EquipoMemberDTO;
import com.redmuqui.platform.proyecto.dto.ProyectoCreateDTO;
import com.redmuqui.platform.proyecto.dto.ProyectoResponseDTO;
import com.redmuqui.platform.proyecto.dto.ProyectoUpdateDTO;
import com.redmuqui.platform.proyecto.service.ProyectoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/proyectos")
@RequiredArgsConstructor
@Tag(name = "Proyectos", description = "Gestión de proyectos institucionales (RF-019 a RF-022)")
public class ProyectoController {

    private final ProyectoService service;

    @GetMapping
    @Operation(summary = "Listar proyectos paginados (RF-022)")
    public ResponseEntity<PageResponse<ProyectoResponseDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(service.listar(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de un proyecto")
    public ResponseEntity<ProyectoResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'TECNICO')")
    @Operation(summary = "Crear un nuevo proyecto (RF-019, RF-020)")
    public ResponseEntity<ProyectoResponseDTO> crear(@Valid @RequestBody ProyectoCreateDTO dto) {
        ProyectoResponseDTO creado = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/v1/proyectos/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'TECNICO')")
    @Operation(summary = "Actualizar un proyecto (RF-021)")
    public ResponseEntity<ProyectoResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ProyectoUpdateDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @GetMapping("/{id}/equipo")
    @Operation(summary = "Listar miembros del equipo del proyecto")
    public ResponseEntity<Set<EquipoMemberDTO>> obtenerEquipo(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerEquipo(id));
    }

    @PostMapping("/{id}/equipo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'TECNICO')")
    @Operation(summary = "Agregar miembro al equipo del proyecto")
    public ResponseEntity<Void> agregarMiembro(@PathVariable Long id, @Valid @RequestBody EquipoMemberDTO dto) {
        service.agregarMiembro(id, dto);
        return ResponseEntity.noContent().build();
    }
}
