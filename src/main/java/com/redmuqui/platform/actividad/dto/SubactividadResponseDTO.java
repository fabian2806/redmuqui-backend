package com.redmuqui.platform.actividad.dto;

import com.redmuqui.platform.actividad.entity.EstadoSubactividad;
import com.redmuqui.platform.actividad.entity.EstadoCronograma;
import java.time.LocalDate;
import java.util.List;
import com.redmuqui.platform.documento.dto.DocumentoEntregableResponseDTO;

public record SubactividadResponseDTO(
    Long id,
    String nombre,
    String responsable,
    Double presupuesto,
    Double costoReal,
    String moneda,
    Integer porcentajeAvance,
    Double avancePlanificado,
    Integer hombresInvolucrados,
    Integer mujeresInvolucradas,
    LocalDate fechaInicioPlanificada,
    LocalDate fechaFinPlanificada,
    LocalDate fechaInicioReal,
    LocalDate fechaFinReal,
    Long desfaseDias,
    EstadoCronograma estadoCronograma,
    EstadoSubactividad estado,
    String descripcion,
    List<DocumentoEntregableResponseDTO> documentosEntregables,
    List<SubactividadArchivoResponseDTO> archivosEvidencia,
    List<SubactividadCofinanciamientoResponseDTO> cofinanciadoPor,
    List<CronogramaReprogramacionDTO> reprogramaciones,
    Long idActividad,
    Long idProyecto
) {}
