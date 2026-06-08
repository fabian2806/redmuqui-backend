package com.redmuqui.platform.actividad.dto;

import com.redmuqui.platform.actividad.entity.EstadoSubactividad;
import java.time.LocalDate;
import java.util.List;

public record SubactividadResponseDTO(
    Long id,
    String nombre,
    String responsable,
    Double presupuesto,
    Integer hombresInvolucrados,
    Integer mujeresInvolucradas,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    EstadoSubactividad estado,
    String descripcion,
    List<SubactividadArchivoResponseDTO> archivosEvidencia,
    List<SubactividadCofinanciamientoResponseDTO> cofinanciadoPor
) {}
