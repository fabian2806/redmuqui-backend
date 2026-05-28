package com.redmuqui.platform.proyecto.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InstitucionParticipacionDTO {

    @NotNull
    private Long idInstitucion;

    @Size(max = 100)
    private String tipoParticipacion;
}
