package com.redmuqui.platform.institucion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InstitucionUpdateDTO(
    @NotBlank @Size(max = 200) String nombre,
    String descripcion,
    @Size(max = 100) String tipo
) {}
