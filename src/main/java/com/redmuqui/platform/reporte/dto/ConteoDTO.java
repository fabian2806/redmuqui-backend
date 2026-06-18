package com.redmuqui.platform.reporte.dto;

/**
 * Par etiqueta/cantidad para series de gráficos del dashboard
 * (proyectos por macroregión RF-073, estado de actividades RF-074).
 */
public record ConteoDTO(
    String etiqueta,
    long cantidad
) {}
