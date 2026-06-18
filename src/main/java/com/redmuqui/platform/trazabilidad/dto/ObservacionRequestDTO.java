package com.redmuqui.platform.trazabilidad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ObservacionRequestDTO(
    @NotBlank String descripcion,
    @NotBlank String entidadReferenciada,
    @NotNull Long idEntidadReferenciada,
    com.redmuqui.platform.trazabilidad.entity.CriticidadIncidencia criticidad,
    Long idResponsable
) {
    public ObservacionRequestDTO(
        String descripcion,
        String entidadReferenciada,
        Long idEntidadReferenciada
    ) {
        this(descripcion, entidadReferenciada, idEntidadReferenciada, null, null);
    }
}
