package com.redmuqui.platform.actividad.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SubactividadCofinanciamientoCreateDTO(
    @NotNull(message = "La actividad origen es obligatoria")
    Long actividadId,
    
    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser positivo")
    Double monto
) {}
