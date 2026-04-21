package com.redmuqui.platform.actividad.dto;

import com.redmuqui.platform.actividad.entity.EstadoActividad;

import java.time.LocalDate;
import java.util.Set;

public record ActividadResponseDTO(
    Long id,
    String nombre,
    String descripcion,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    EstadoActividad estado,
    Long idProyecto,
    Set<Long> idResponsables
) {}
