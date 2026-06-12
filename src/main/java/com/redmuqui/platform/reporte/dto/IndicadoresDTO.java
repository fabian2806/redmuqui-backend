package com.redmuqui.platform.reporte.dto;

/**
 * Indicadores globales del dashboard (RF-069).
 *
 * Cifras agregadas en servidor sobre los datos que ya capturan los módulos de
 * proyectos, actividades y documentos. Los beneficiarios desagregados por género
 * y el presupuesto provienen del nivel de subactividad/proyecto.
 */
public record IndicadoresDTO(
    long proyectosActivos,
    long proyectosEnRiesgo,
    double presupuestoTotal,
    double avancePromedio,
    long beneficiariosHombres,
    long beneficiariosMujeres,
    long documentosPublicados,
    long documentosPendientes
) {}
