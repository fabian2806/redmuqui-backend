package com.redmuqui.platform.actividad.dto;

import com.redmuqui.platform.actividad.entity.EstadoHito;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record HitoCreateDTO(
    @NotBlank String nombre,
    String descripcion,
    @NotNull LocalDate fechaClave,
    EstadoHito estado,
    @NotNull Long idProyecto
) {}
