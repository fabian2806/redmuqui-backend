package com.redmuqui.platform.territorio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TerritorioDTO(
    Long id,
    @NotBlank @Size(max = 200) String nombre,
    String descripcion
) {}
