package com.redmuqui.platform.reporte.dto;

import com.redmuqui.platform.proyecto.entity.EstadoProyecto;

/**
 * Resumen liviano de proyecto para tableros de avance.
 */
public record ProyectoAvanceDTO(
    Long id,
    String nombre,
    String macroregion,
    EstadoProyecto estado,
    double porcentajeAvance
) {}
