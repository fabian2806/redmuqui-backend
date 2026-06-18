package com.redmuqui.platform.actividad.dto;

import com.redmuqui.platform.actividad.entity.EstadoCronograma;
import com.redmuqui.platform.actividad.entity.EstadoFase;

import java.time.LocalDate;
import java.util.List;

public record FaseResponseDTO(
    Long id,
    String nombre,
    String descripcion,
    LocalDate fechaInicioPlanificada,
    LocalDate fechaFinPlanificada,
    LocalDate fechaInicioReal,
    LocalDate fechaFinReal,
    EstadoFase estado,
    Double porcentajeAvance,
    Long desfaseDias,
    EstadoCronograma estadoCronograma,
    Long idProyecto,
    Integer totalActividades,
    Integer actividadesFinalizadas,
    List<CronogramaReprogramacionDTO> reprogramaciones
) {}
