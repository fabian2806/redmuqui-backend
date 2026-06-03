package com.redmuqui.platform.actividad.dto;

import com.redmuqui.platform.actividad.entity.EstadoActividad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.Set;

public record ActividadUpdateDTO(
    @NotBlank String nombre,
    String descripcion,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    EstadoActividad estado,
    @NotNull Long idProyecto,
    Set<Long> idResponsables
) {}
