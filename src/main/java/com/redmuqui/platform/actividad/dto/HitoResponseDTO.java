package com.redmuqui.platform.actividad.dto;

import com.redmuqui.platform.actividad.entity.EstadoHito;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record HitoResponseDTO(
    Long id,
    String nombre,
    String descripcion,
    LocalDate fechaClave,
    EstadoHito estado,
    Long idProyecto,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaModificacion
) {}
