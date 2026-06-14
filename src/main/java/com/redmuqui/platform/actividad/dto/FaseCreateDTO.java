package com.redmuqui.platform.actividad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record FaseCreateDTO(
    @NotBlank @Size(max = 255) String nombre,
    String descripcion,
    @NotNull LocalDate fechaInicioPlanificada,
    @NotNull LocalDate fechaFinPlanificada,
    LocalDate fechaInicioReal,
    LocalDate fechaFinReal,
    String motivoReprogramacion
) {}
