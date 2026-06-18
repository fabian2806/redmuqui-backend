package com.redmuqui.platform.trazabilidad.dto;

import com.redmuqui.platform.trazabilidad.entity.EstadoObservacion;

import java.time.LocalDateTime;

public record ObservacionResponseDTO(
    Long id,
    String descripcion,
    LocalDateTime fecha,
    EstadoObservacion estado,
    com.redmuqui.platform.trazabilidad.entity.CriticidadIncidencia criticidad,
    LocalDateTime fechaVencimiento,
    LocalDateTime fechaResolucion,
    String comentarioResolucion,
    Long idUsuarioResolucion,
    String nombreUsuarioResolucion,
    String entidadReferenciada,
    Long idEntidadReferenciada,
    Long idUsuario,
    String nombreUsuario,
    String emailUsuario,
    Long idResponsable,
    String nombreResponsable
) {}
