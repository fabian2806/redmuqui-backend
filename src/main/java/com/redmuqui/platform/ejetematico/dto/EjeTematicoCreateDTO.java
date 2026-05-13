package com.redmuqui.platform.ejetematico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EjeTematicoCreateDTO(
    @NotBlank @Size(max = 200) String nombre,
    String descripcion
) {}
