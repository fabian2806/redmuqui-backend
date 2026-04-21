package com.redmuqui.platform.proyecto.dto;

import com.redmuqui.platform.proyecto.entity.EstadoProyecto;
import com.redmuqui.platform.usuario.dto.UsuarioSummaryDTO;

import java.time.LocalDate;
import java.util.Set;

public record ProyectoResponseDTO(
    Long id,
    String nombre,
    String codigoInterno,
    String descripcion,
    String objetivoGeneral,
    LocalDate fechaInicio,
    LocalDate fechaFinEstimada,
    EstadoProyecto estado,
    Integer nivelPrioridad,
    Double porcentajeAvance,
    Double presupuesto,
    String nombreMacroregion,
    Long idMacroregion,
    String nombreEjeTematico,
    Long idEjeTematico,
    UsuarioSummaryDTO responsablePrincipal,
    Set<TerritorioRefDTO> territorios
) {
    public record TerritorioRefDTO(Long id, String nombre) {}
}
