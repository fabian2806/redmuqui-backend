package com.redmuqui.platform.estado.dto;

import com.redmuqui.platform.estado.entity.ModuloEstado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EstadoCreateDTO(
    @NotBlank @Size(max = 200) String nombre,
    @NotBlank @Size(max = 100) String codigo,
    String descripcion,
    @NotNull ModuloEstado modulo,
    Boolean activo
) {}
