package com.redmuqui.platform.trazabilidad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ObservacionCreateDTO(
    @NotBlank String descripcion,
    @NotBlank String entidadReferenciada,
    @NotNull Long idEntidadRef
) {}
