package com.redmuqui.platform.estado.dto;

import com.redmuqui.platform.estado.entity.ModuloEstado;

public record EstadoResponseDTO(
    Long id,
    String nombre,
    String codigo,
    String descripcion,
    ModuloEstado modulo,
    Boolean activo
) {}
