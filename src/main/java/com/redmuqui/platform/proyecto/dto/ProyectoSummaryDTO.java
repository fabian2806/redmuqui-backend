package com.redmuqui.platform.proyecto.dto;

import com.redmuqui.platform.proyecto.entity.EstadoProyecto;

public record ProyectoSummaryDTO(
    Long id,
    String nombre,
    String codigoInterno,
    EstadoProyecto estado,
    Double porcentajeAvance
) {}
