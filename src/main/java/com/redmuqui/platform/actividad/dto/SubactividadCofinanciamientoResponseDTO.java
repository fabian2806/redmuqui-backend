package com.redmuqui.platform.actividad.dto;

public record SubactividadCofinanciamientoResponseDTO(
    Long actividadId,
    String actividadNombre,
    Long proyectoId,
    String proyectoNombre,
    Double monto,
    String justificacion
) {}
