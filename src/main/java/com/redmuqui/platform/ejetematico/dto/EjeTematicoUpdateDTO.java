package com.redmuqui.platform.ejetematico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EjeTematicoUpdateDTO(
    @NotBlank @Size(max = 200) String nombre,
    String descripcion
) {}
