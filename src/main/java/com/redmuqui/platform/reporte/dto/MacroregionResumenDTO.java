package com.redmuqui.platform.reporte.dto;

/**
 * Agregado geografico por macroregion.
 */
public record MacroregionResumenDTO(
    String nombre,
    long totalProyectos,
    long activos,
    long finalizados,
    long instituciones
) {}
