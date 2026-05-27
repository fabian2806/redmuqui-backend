package com.redmuqui.platform.actividad.dto;

import com.redmuqui.platform.actividad.entity.EstadoActividad;

import java.time.LocalDate;
import java.util.Set;
import java.util.List;

public record ActividadResponseDTO(
    Long id,
    String nombre,
    String descripcion,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    EstadoActividad estado,
    Integer porcentajeAvance,
    Long idProyecto,
    Set<Long> idResponsables,
    List<SubactividadResponseDTO> subactividades
) {}
