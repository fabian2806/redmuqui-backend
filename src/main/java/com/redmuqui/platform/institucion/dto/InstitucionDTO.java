package com.redmuqui.platform.institucion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InstitucionDTO(
    Long id,
    @NotBlank @Size(max = 200) String nombre,
    @Size(max = 100) String tipo
) {}
