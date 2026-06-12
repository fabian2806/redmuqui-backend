package com.redmuqui.platform.reporte.dto;

import java.time.LocalDate;

/**
 * Proyecto activo identificado como "en riesgo" para el dashboard (RF-071).
 *
 * La regla que clasifica el riesgo vive en ReporteService; este DTO transporta
 * los insumos que la justifican para que el frontend los muestre y coloree.
 * Reutilizable por el futuro Semáforo de portafolio.
 */
public record ProyectoRiesgoDTO(
    Long id,
    String nombre,
    String codigoInterno,
    double porcentajeAvance,
    LocalDate fechaFinEstimada,
    Long diasRestantes,
    long hitosVencidos
) {}
