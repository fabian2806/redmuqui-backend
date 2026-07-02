package com.redmuqui.platform.tipodocumento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TipoDocumentoUpdateDTO(
    @NotBlank @Size(max = 200) String nombre,
    @NotBlank @Size(max = 100) String codigo,
    String descripcion,
    @NotNull Boolean activo
) {}
