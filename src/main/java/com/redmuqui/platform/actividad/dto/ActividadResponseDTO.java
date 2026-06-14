package com.redmuqui.platform.actividad.dto;

import com.redmuqui.platform.actividad.entity.EstadoActividad;
import com.redmuqui.platform.actividad.entity.EstadoCronograma;

import java.time.LocalDate;
import java.util.Set;
import java.util.List;

public record ActividadResponseDTO(
    Long id,
    String nombre,
    String descripcion,
    LocalDate fechaInicioPlanificada,
    LocalDate fechaFinPlanificada,
    EstadoActividad estado,
    Integer porcentajeAvance,
    Double avancePlanificado,
    Double costoEstimado,
    Double costoReal,
    String moneda,
    LocalDate fechaInicioReal,
    LocalDate fechaFinReal,
    Long desfaseDias,
    EstadoCronograma estadoCronograma,
    List<CronogramaReprogramacionDTO> reprogramaciones,
    Long idProyecto,
    Long idFase,
    String nombreFase,
    Long idHito,
    String nombreHito,
    Set<Long> idResponsables,
    List<SubactividadResponseDTO> subactividades
) {
    public ActividadResponseDTO(
        Long id, String nombre, String descripcion, LocalDate fechaInicioPlanificada, LocalDate fechaFinPlanificada,
        EstadoActividad estado, Integer porcentajeAvance, Long idProyecto, Long idHito,
        String nombreHito, Set<Long> idResponsables, List<SubactividadResponseDTO> subactividades
    ) {
        this(id, nombre, descripcion, fechaInicioPlanificada, fechaFinPlanificada, estado, porcentajeAvance,
            0D, 0D, 0D, "PEN", null, null, null, EstadoCronograma.PENDIENTE,
            List.of(), idProyecto, null, null, idHito, nombreHito, idResponsables, subactividades);
    }
}
