package com.redmuqui.platform.proyecto.dto;

import jakarta.validation.constraints.NotNull;

public record EquipoMemberDTO(
    @NotNull Long idUsuario,
    String rolEnProyecto
) {}
