package com.redmuqui.platform.tipodocumento.dto;

public record TipoDocumentoResponseDTO(
    Long id,
    String nombre,
    String codigo,
    String descripcion,
    Boolean activo
) {}
