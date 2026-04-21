package com.redmuqui.platform.macroregion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MacroregionDTO(
    Long id,
    @NotBlank @Size(max = 100) String nombre,
    String descripcion
) {}
