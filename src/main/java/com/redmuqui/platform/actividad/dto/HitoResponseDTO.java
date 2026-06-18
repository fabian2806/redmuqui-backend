package com.redmuqui.platform.actividad.dto;

import com.redmuqui.platform.actividad.entity.EstadoHito;
import com.redmuqui.platform.actividad.entity.EstadoCronograma;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record HitoResponseDTO(
    Long id,
    String nombre,
    String descripcion,
    LocalDate fechaClave,
    EstadoHito estado,
    Long idProyecto,
    Long idFase,
    String nombreFase,
    List<Long> idsActividades,
    Double porcentajeAvance,
    LocalDate fechaInicioPlanificada,
    LocalDate fechaFinPlanificada,
    Long duracionDias,
    Integer totalActividades,
    Integer actividadesFinalizadas,
    LocalDate fechaCumplimientoReal,
    Long desfaseDias,
    EstadoCronograma estadoCronograma,
    List<CronogramaReprogramacionDTO> reprogramaciones,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaModificacion
) {
    public HitoResponseDTO(
        Long id,
        String nombre,
        String descripcion,
        LocalDate fechaClave,
        EstadoHito estado,
        Long idProyecto,
        Double porcentajeAvance,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Long duracionDias,
        Integer totalActividades,
        Integer actividadesFinalizadas,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
    ) {
        this(
            id,
            nombre,
            descripcion,
            fechaClave,
            estado,
            idProyecto,
            null,
            null,
            List.of(),
            porcentajeAvance,
            fechaInicio,
            fechaFin,
            duracionDias,
            totalActividades,
            actividadesFinalizadas,
            null,
            null,
            EstadoCronograma.PENDIENTE,
            List.of(),
            fechaCreacion,
            fechaModificacion
        );
    }
}
