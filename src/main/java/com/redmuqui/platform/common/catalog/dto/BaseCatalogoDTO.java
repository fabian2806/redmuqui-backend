package com.redmuqui.platform.common.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public interface BaseCatalogoDTO {
    Long id();
    @NotBlank @Size(max = 200) String nombre();
    String descripcion();
    Boolean activo();
}
