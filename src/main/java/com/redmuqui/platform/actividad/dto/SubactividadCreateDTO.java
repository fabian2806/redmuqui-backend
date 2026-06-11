package com.redmuqui.platform.actividad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

public record SubactividadCreateDTO(
    @NotBlank(message = "El nombre es obligatorio")
    String nombre,
    
    @NotNull(message = "El responsable es obligatorio")
    Long idResponsable,
    
    @PositiveOrZero(message = "El presupuesto no puede ser negativo")
    Double presupuesto,
    
    @PositiveOrZero(message = "Los hombres involucrados no pueden ser negativos")
    Integer hombresInvolucrados,
    
    @PositiveOrZero(message = "Las mujeres involucradas no pueden ser negativas")
    Integer mujeresInvolucradas,
    
    LocalDate fechaInicio,
    LocalDate fechaFin,
    String descripcion
) {}
