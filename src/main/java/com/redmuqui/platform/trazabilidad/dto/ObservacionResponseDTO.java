package com.redmuqui.platform.trazabilidad.dto;

import com.redmuqui.platform.trazabilidad.entity.EstadoObservacion;

import java.time.LocalDateTime;

public record ObservacionResponseDTO(
    Long id,
    String descripcion,
    LocalDateTime fecha,
    EstadoObservacion estado,
    String entidadReferenciada,
    Long idEntidadRef,
    Long idUsuario,
    String nombreUsuario
) {}
