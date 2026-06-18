package com.redmuqui.platform.reporte.dto;

/**
 * Serie agregada con cantidad y presupuesto, usada para ejes tematicos.
 */
public record ConteoPresupuestoDTO(
    String etiqueta,
    long cantidad,
    double presupuesto
) {}
