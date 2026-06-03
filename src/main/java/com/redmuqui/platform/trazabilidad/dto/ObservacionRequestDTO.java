package com.redmuqui.platform.trazabilidad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ObservacionRequestDTO(
    @NotBlank String descripcion,
    @NotBlank String entidadReferenciada,
    @NotNull Long idEntidadReferenciada
) {}
