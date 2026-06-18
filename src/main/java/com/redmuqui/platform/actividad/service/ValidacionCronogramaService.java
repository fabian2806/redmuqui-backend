package com.redmuqui.platform.actividad.service;

import com.redmuqui.platform.actividad.entity.Actividad;
import com.redmuqui.platform.actividad.entity.Fase;
import com.redmuqui.platform.actividad.entity.Subactividad;
import com.redmuqui.platform.actividad.repository.ActividadRepository;
import com.redmuqui.platform.actividad.repository.SubactividadRepository;
import com.redmuqui.platform.common.exception.BusinessException;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ValidacionCronogramaService {

    private final ActividadRepository actividadRepository;
    private final SubactividadRepository subactividadRepository;

    public void validarRango(LocalDate inicio, LocalDate fin, String entidad) {
        if (inicio == null || fin == null) {
            throw new BusinessException("Las fechas planificadas de " + entidad + " son obligatorias");
        }
        if (fin.isBefore(inicio)) {
            throw new BusinessException("La fecha fin planificada de " + entidad
                + " no puede ser anterior a la fecha inicio planificada");
        }
    }

    public void validarFaseEnProyecto(
        Proyecto proyecto,
        LocalDate inicio,
        LocalDate fin
    ) {
        validarRango(inicio, fin, "la fase");
        if (inicio.isBefore(proyecto.getFechaInicio()) || fin.isAfter(proyecto.getFechaFinEstimada())) {
            throw new BusinessException("La fase debe estar dentro del periodo planificado del proyecto");
        }
    }

    public void validarActividadEnFase(
        Fase fase,
        LocalDate inicio,
        LocalDate fin
    ) {
        validarRango(inicio, fin, "la actividad");
        if (inicio.isBefore(fase.getFechaInicioPlanificada())
            || fin.isAfter(fase.getFechaFinPlanificada())) {
            throw new BusinessException("La actividad debe estar dentro del periodo planificado de la fase");
        }
    }

    public void validarSubactividadEnActividad(
        Actividad actividad,
        LocalDate inicio,
        LocalDate fin
    ) {
        validarRango(inicio, fin, "la subactividad");
        if (inicio.isBefore(actividad.getFechaInicioPlanificada())
            || fin.isAfter(actividad.getFechaFinPlanificada())) {
            throw new BusinessException("La subactividad debe estar dentro del periodo planificado de la actividad");
        }
    }

    public void validarCambioRangoFase(Fase fase, LocalDate inicio, LocalDate fin) {
        validarFaseEnProyecto(fase.getProyecto(), inicio, fin);
        boolean dejaActividadFuera = actividadRepository.findByFaseIdOrderByFechaInicioPlanificadaAscIdAsc(fase.getId())
            .stream()
            .anyMatch(a -> a.getFechaInicioPlanificada().isBefore(inicio)
                || a.getFechaFinPlanificada().isAfter(fin));
        if (dejaActividadFuera) {
            throw new BusinessException("No se puede reducir el periodo de la fase porque dejaría actividades fuera");
        }
    }

    public void validarCambioRangoActividad(
        Actividad actividad,
        Fase faseDestino,
        LocalDate inicio,
        LocalDate fin
    ) {
        validarActividadEnFase(faseDestino, inicio, fin);
        boolean dejaSubactividadFuera = subactividadRepository.findByActividadId(actividad.getId())
            .stream()
            .anyMatch(s -> s.getFechaInicioPlanificada().isBefore(inicio)
                || s.getFechaFinPlanificada().isAfter(fin));
        if (dejaSubactividadFuera) {
            throw new BusinessException(
                "No se puede reducir el periodo de la actividad porque dejaría subactividades fuera"
            );
        }
    }

    public void validarFechaReal(LocalDate fechaFinReal, LocalDate fechaInicioReal, String entidad) {
        if (fechaInicioReal != null && fechaFinReal != null && fechaFinReal.isBefore(fechaInicioReal)) {
            throw new BusinessException("La fecha fin real de " + entidad
                + " no puede ser anterior a su fecha inicio real");
        }
    }

    public void validarFechaReal(
        LocalDate fechaFinReal,
        LocalDate fechaInicioReal,
        LocalDate fechaInicioPlanificada,
        String entidad
    ) {
        validarFechaReal(fechaFinReal, fechaInicioReal, entidad);
        if (fechaInicioReal != null
            && fechaInicioPlanificada != null
            && fechaInicioReal.isBefore(fechaInicioPlanificada)) {
            throw new BusinessException("La fecha inicio real de " + entidad
                + " no puede ser anterior a su fecha inicio planificada");
        }
    }
}
