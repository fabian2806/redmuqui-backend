package com.redmuqui.platform.moneda.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MonedaUpdateDTO(
    @NotBlank @Size(max = 200) String nombre,
    @NotBlank @Size(max = 3) String codigo,
    @NotBlank @Size(max = 10) String simbolo,
    @NotNull Boolean activo
) {}
