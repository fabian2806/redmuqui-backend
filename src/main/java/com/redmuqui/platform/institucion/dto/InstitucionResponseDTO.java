package com.redmuqui.platform.institucion.dto;

import com.redmuqui.platform.common.catalog.dto.BaseCatalogoDTO;

public record InstitucionResponseDTO(
    Long id,
    String nombre,
    String descripcion,
    String tipo
) implements BaseCatalogoDTO {}
