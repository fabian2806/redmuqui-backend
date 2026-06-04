package com.redmuqui.platform.proyecto.controller;

import com.redmuqui.platform.common.dto.PageResponse;
import com.redmuqui.platform.proyecto.dto.AsociarInstitucionesDTO;
import com.redmuqui.platform.proyecto.dto.EquipoMemberDTO;
import com.redmuqui.platform.proyecto.dto.InstitucionParticipacionDTO;
import com.redmuqui.platform.proyecto.dto.ProyectoCreateDTO;
import com.redmuqui.platform.proyecto.dto.ProyectoResponseDTO;
import com.redmuqui.platform.proyecto.dto.ProyectoTerritorioRequestDTO;
import com.redmuqui.platform.proyecto.dto.ProyectoUpdateDTO;
import com.redmuqui.platform.proyecto.entity.EstadoProyecto;
import com.redmuqui.platform.proyecto.service.ProyectoService;
import com.redmuqui.platform.actividad.service.ActividadService;
import com.redmuqui.platform.actividad.dto.ActividadResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/proyectos")
@RequiredArgsConstructor
@Tag(name = "Proyectos", description = "Gestión de proyectos institucionales (RF-019 a RF-022)")
public class ProyectoController {

    private final ProyectoService service;
    private final ActividadService actividadService;

    @GetMapping
    @PreAuthorize("hasAuthority('PROYECTOS_READ')")
    @Operation(summary = "Listar proyectos paginados con filtros (RF-082 a RF-087)")
    public ResponseEntity<PageResponse<ProyectoResponseDTO>> listar(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) EstadoProyecto estado,
        @RequestParam(required = false) Long idMacroregion,
        @RequestParam(required = false) Long idEjeTematico,
        @RequestParam(required = false) Long idInstitucion,
        @RequestParam(required = false) Integer anio,
        Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponse.from(
            service.listar(q, estado, idMacroregion, idEjeTematico, idInstitucion, anio, pageable)
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROYECTOS_READ')")
    @Operation(summary = "Obtener detalle de un proyecto")
    public ResponseEntity<ProyectoResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @GetMapping("/ultimo-codigo")
    @Operation(summary = "Obtener el último código de proyecto registrado")
    public ResponseEntity<Map<String, String>> obtenerUltimoCodigo() {
        String ultimoCodigo = service.obtenerUltimoCodigo();
        return ResponseEntity.ok(Map.of("ultimoCodigo", ultimoCodigo != null ? ultimoCodigo : ""));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROYECTOS_CREATE')")
    @Operation(summary = "Crear un nuevo proyecto (RF-019, RF-020)")
    public ResponseEntity<ProyectoResponseDTO> crear(@Valid @RequestBody ProyectoCreateDTO dto) {
        ProyectoResponseDTO creado = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/v1/proyectos/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROYECTOS_UPDATE')")
    @Operation(summary = "Actualizar un proyecto (RF-021)")
    public ResponseEntity<ProyectoResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ProyectoUpdateDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @GetMapping("/{id}/equipo")
    @PreAuthorize("hasAuthority('PROYECTOS_READ')")
    @Operation(summary = "Listar miembros del equipo del proyecto")
    public ResponseEntity<Set<EquipoMemberDTO>> obtenerEquipo(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerEquipo(id));
    }

    @GetMapping("/{id}/actividades")
    @PreAuthorize("hasAuthority('PROYECTOS_READ')")
    @Operation(summary = "Listar actividades y subactividades de un proyecto")
    public ResponseEntity<java.util.List<ActividadResponseDTO>> obtenerActividades(@PathVariable Long id) {
        return ResponseEntity.ok(actividadService.listarPorProyecto(id));
    }

    @PostMapping("/{id}/equipo")
    @PreAuthorize("hasAuthority('PROYECTOS_UPDATE')")
    @Operation(summary = "Agregar miembro al equipo del proyecto")
    public ResponseEntity<Void> agregarMiembro(@PathVariable Long id, @Valid @RequestBody EquipoMemberDTO dto) {
        service.agregarMiembro(id, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/equipo/{idUsuario}")
    @PreAuthorize("hasAuthority('PROYECTOS_UPDATE')")
    @Operation(summary = "Eliminar miembro del equipo del proyecto")
    public ResponseEntity<Void> eliminarMiembro(@PathVariable Long id, @PathVariable Long idUsuario) {
        service.eliminarMiembro(id, idUsuario);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/equipo/{idUsuario}")
    @PreAuthorize("hasAuthority('PROYECTOS_UPDATE')")
    @Operation(summary = "Actualizar rol de miembro del equipo del proyecto")
    public ResponseEntity<Void> actualizarRolMiembro(@PathVariable Long id, @PathVariable Long idUsuario, @RequestParam String nuevoRol) {
        service.actualizarRolMiembro(id, idUsuario, nuevoRol);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('PROYECTOS_UPDATE')")
    @Operation(summary = "Actualizar estado de un proyecto (RF-031)")
    public ResponseEntity<ProyectoResponseDTO> cambiarEstado(@PathVariable Long id, @RequestParam EstadoProyecto estado) {
        return ResponseEntity.ok(service.cambiarEstado(id, estado));
    }

    @PatchMapping("/{id}/avance")
    @PreAuthorize("hasAuthority('PROYECTOS_UPDATE')")
    @Operation(summary = "Actualizar porcentaje de avance de un proyecto (RF-032)")
    public ResponseEntity<ProyectoResponseDTO> actualizarAvance(@PathVariable Long id, @RequestParam Double porcentajeAvance) {
        return ResponseEntity.ok(service.actualizarAvance(id, porcentajeAvance));
    }

    @GetMapping("/{id}/instituciones")
    @PreAuthorize("hasAuthority('PROYECTOS_READ')")
    @Operation(summary = "Listar instituciones asociadas al proyecto")
    public ResponseEntity<Set<InstitucionParticipacionDTO>> obtenerInstituciones(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerInstituciones(id));
    }

    @PostMapping("/{id}/instituciones")
    @PreAuthorize("hasAuthority('PROYECTOS_UPDATE')")
    @Operation(summary = "Asociar una o más instituciones miembro a un proyecto")
    public ResponseEntity<Void> asociarInstituciones(
        @PathVariable Long id,
        @Valid @RequestBody AsociarInstitucionesDTO dto
    ) {
        service.asociarInstituciones(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/territorios")
    @PreAuthorize("hasAuthority('PROYECTOS_UPDATE')")
    @Operation(summary = "Asociar uno o más territorios a un proyecto")
    public ResponseEntity<Void> asociarTerritorios(
        @PathVariable Long id,
        @Valid @RequestBody ProyectoTerritorioRequestDTO dto
    ) {
        service.asociarTerritorios(id, dto);
        return ResponseEntity.noContent().build();
    }
}
