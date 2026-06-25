package com.redmuqui.platform.actividad.dto;

import java.util.List;

public record CofinanciamientoDisponibleDTO(
    String moneda,
    Long excludeProyectoId,
    List<ProyectoDisponibleDTO> proyectos
) {
    public record ProyectoDisponibleDTO(
        Long proyectoId,
        String proyectoNombre,
        String moneda,
        List<ActividadDisponibleDTO> actividades
    ) {}

    public record ActividadDisponibleDTO(
        Long actividadId,
        String actividadNombre,
        Double presupuesto,
        Double presupuestoComprometido,
        Double presupuestoDisponible
    ) {}
}
