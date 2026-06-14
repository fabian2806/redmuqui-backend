package com.redmuqui.platform.actividad.dto;

public record SubactividadArchivoResponseDTO(
    Long id,
    String nombre,
    String url,
    com.redmuqui.platform.actividad.entity.EstadoEvidencia estado,
    Long idUsuarioCarga,
    String usuarioCarga
) {}
