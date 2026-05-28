package com.redmuqui.platform.proyecto.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class ProyectoTerritorioRequestDTO {

    @NotEmpty
    private Set<Long> territoriosIds;
}
