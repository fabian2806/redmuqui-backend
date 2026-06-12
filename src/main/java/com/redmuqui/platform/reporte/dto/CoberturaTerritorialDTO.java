package com.redmuqui.platform.reporte.dto;

/**
 * Cobertura de la red por unidad territorial (Sprint 4 ④, Mapa Territorial).
 *
 * Una fila por territorio del nivel solicitado (hoy: departamento), con sus
 * cifras agregadas. {@code codigo} es el UBIGEO, que el frontend cruza con la
 * geometría del mapa. Los territorios sin actividad llegan igual, en cero.
 */
public record CoberturaTerritorialDTO(
    Long idTerritorio,
    String codigo,
    String nombre,
    String tipo,
    long proyectos,
    double presupuesto,
    long beneficiarios,
    long instituciones
) {}
