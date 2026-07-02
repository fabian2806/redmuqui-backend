package com.redmuqui.platform.territorio.dto;

import com.redmuqui.platform.common.catalog.dto.BaseCatalogoDTO;
import lombok.Data;

@Data
public class TerritorioResponseDTO implements BaseCatalogoDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private Boolean activo;

    @Override
    public Long id() {
        return id;
    }

    @Override
    public String nombre() {
        return nombre;
    }

    @Override
    public String descripcion() {
        return descripcion;
    }

    @Override
    public Boolean activo() {
        return activo;
    }
}
