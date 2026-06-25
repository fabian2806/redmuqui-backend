package com.redmuqui.platform.reporte.dto;

public record PresupuestoPorMonedaDTO(
    String moneda,
    double monto,
    long proyectos
) {}
