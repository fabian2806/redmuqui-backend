package com.redmuqui.platform.territorio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TerritorioCreateDTO {

    @NotBlank
    @Size(max = 200)
    private String nombre;

    @NotBlank
    @Size(max = 10000)
    private String descripcion;
}
