package com.redmuqui.platform.actividad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SubactividadCofinanciamientoCreateDTO(
    @NotNull(message = "La actividad origen es obligatoria")
    Long actividadId,
    
    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser positivo")
    Double monto,

    @NotBlank(message = "La justificacion es obligatoria")
    @Size(max = 1000, message = "La justificacion no puede superar 1000 caracteres")
    String justificacion
) {}
