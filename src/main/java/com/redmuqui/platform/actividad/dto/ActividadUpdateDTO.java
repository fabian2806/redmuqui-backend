package com.redmuqui.platform.actividad.dto;

import com.redmuqui.platform.actividad.entity.EstadoActividad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public record ActividadUpdateDTO(
    @NotBlank @Size(max = 255) String nombre,
    String descripcion,
    LocalDate fechaInicioPlanificada,
    LocalDate fechaFinPlanificada,
    EstadoActividad estado,
    @NotNull Long idProyecto,
    @NotNull Long idFase,
    Long idHito,
    @PositiveOrZero(message = "El presupuesto no puede ser negativo")
    Double presupuesto,
    Set<Long> idResponsables,
    LocalDate fechaInicioReal,
    LocalDate fechaFinReal,
    String motivoReprogramacion
) {}
