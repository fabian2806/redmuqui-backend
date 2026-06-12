package com.redmuqui.platform.institucion.dto;

import com.redmuqui.platform.common.catalog.dto.BaseCatalogoDTO;
import jakarta.validation.constraints.Size;

public record InstitucionResponseDTO(
    Long id,
    String nombre,
    String descripcion,
    @Size(max = 100) String tipo
) implements BaseCatalogoDTO {}
