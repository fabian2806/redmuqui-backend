package com.redmuqui.platform.macroregion.dto;

import com.redmuqui.platform.common.catalog.dto.BaseCatalogoDTO;

public record MacroregionResponseDTO(
    Long id,
    String nombre,
    String descripcion,
    Boolean activo
) implements BaseCatalogoDTO {}