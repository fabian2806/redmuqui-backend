package com.redmuqui.platform.actividad.dto;

public record CofinanciamientoSalienteDTO(
    Long subactividadId,
    String subactividadNombre,
    Long proyectoDestinoId,
    String proyectoDestinoNombre,
    Double monto,
    String justificacion
) {}
