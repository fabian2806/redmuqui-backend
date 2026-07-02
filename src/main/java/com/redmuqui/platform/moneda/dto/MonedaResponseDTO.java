package com.redmuqui.platform.moneda.dto;

public record MonedaResponseDTO(
    Long id,
    String nombre,
    String codigo,
    String simbolo,
    Boolean activo
) {}
