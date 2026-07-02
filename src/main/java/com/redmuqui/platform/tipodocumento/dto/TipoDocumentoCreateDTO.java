package com.redmuqui.platform.tipodocumento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TipoDocumentoCreateDTO(
    @NotBlank @Size(max = 200) String nombre,
    @NotBlank @Size(max = 100) String codigo,
    String descripcion,
    Boolean activo
) {}
