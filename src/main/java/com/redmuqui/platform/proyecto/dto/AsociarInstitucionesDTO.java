package com.redmuqui.platform.proyecto.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AsociarInstitucionesDTO {

    @NotEmpty
    @Valid
    private List<InstitucionParticipacionDTO> instituciones;
}
