package com.redmuqui.platform.proyecto.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AsociarInstitucionesDTO {

    @NotNull
    @Valid
    private List<InstitucionParticipacionDTO> instituciones;
}
