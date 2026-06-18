package com.redmuqui.platform.trazabilidad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResolverIncidenciaRequestDTO(
    @NotBlank(message = "El comentario de resolución es obligatorio")
    @Size(max = 2000, message = "El comentario no puede superar los 2000 caracteres")
    String comentario
) {}
