package com.redmuqui.platform.actividad.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CronogramaReprogramacionDTO(
    Long id,
    LocalDate fechaInicioAnterior,
    LocalDate fechaFinAnterior,
    LocalDate fechaInicioNueva,
    LocalDate fechaFinNueva,
    String motivo,
    Long idUsuario,
    String nombreUsuario,
    LocalDateTime fechaCreacion
) {}
