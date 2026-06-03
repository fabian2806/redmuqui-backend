package com.redmuqui.platform.proyecto.dto;

import com.redmuqui.platform.proyecto.entity.EstadoProyecto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public record ProyectoUpdateDTO(
    @NotBlank @Size(max = 255) String nombre,
    @NotBlank @Size(max = 50) String codigoInterno,
    String descripcion,
    String objetivoGeneral,
    @NotNull LocalDate fechaInicio,
    LocalDate fechaFinEstimada,
    EstadoProyecto estado,
    Integer nivelPrioridad,
    Double porcentajeAvance,
    Double presupuesto,
    Long idMacroregion,
    Set<Long> idMacroregiones,
    Long idEjeTematico,
    Long idResponsablePrincipal,
    Set<Long> idTerritorios
) {}
