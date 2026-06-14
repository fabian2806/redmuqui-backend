package com.redmuqui.platform.actividad.service;

import com.redmuqui.platform.actividad.entity.*;
import com.redmuqui.platform.actividad.repository.*;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.proyecto.repository.ProyectoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AvanceProyectoService {

    private final ActividadRepository actividadRepository;
    private final HitoRepository hitoRepository;
    private final FaseRepository faseRepository;
    private final SubactividadRepository subactividadRepository;
    private final ProyectoRepository proyectoRepository;

    public long duracionActividad(Actividad actividad) {
        return duracion(
            actividad.getFechaInicioPlanificada(),
            actividad.getFechaFinPlanificada()
        );
    }

    public long duracionSubactividad(Subactividad subactividad) {
        return duracion(
            subactividad.getFechaInicioPlanificada(),
            subactividad.getFechaFinPlanificada()
        );
    }

    public long duracionFase(Fase fase) {
        return duracion(fase.getFechaInicioPlanificada(), fase.getFechaFinPlanificada());
    }

    private long duracion(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null || fin.isBefore(inicio)) return 1L;
        return ChronoUnit.DAYS.between(inicio, fin) + 1L;
    }

    @Transactional
    public void recalcularActividad(Long actividadId) {
        Actividad actividad = actividadRepository.findById(actividadId).orElse(null);
        if (actividad == null) return;
        sincronizarActividadDesdeSubactividades(actividad);
        actividadRepository.save(actividad);
        recalcularFase(actividad.getFase().getId());
        recalcularHito(actividad.getHito());
        recalcularProyecto(actividad.getProyecto().getId());
    }

    private void sincronizarActividadDesdeSubactividades(Actividad actividad) {
        List<Subactividad> subactividades = subactividadRepository.findByActividadId(actividad.getId());
        if (subactividades.isEmpty()) {
            if (actividad.getEstado() == EstadoActividad.FINALIZADA) {
                actividad.setPorcentajeAvance(100);
            } else if (actividad.getPorcentajeAvance() == null) {
                actividad.setPorcentajeAvance(0);
            }
            return;
        }

        long pesoTotal = subactividades.stream().mapToLong(this::duracionSubactividad).sum();
        double sumaPonderada = subactividades.stream()
            .mapToDouble(s -> porcentajeSubactividad(s) * duracionSubactividad(s))
            .sum();
        int avance = pesoTotal == 0 ? 0 : (int) Math.round(sumaPonderada / pesoTotal);
        long finalizadas = subactividades.stream()
            .filter(s -> s.getEstado() == EstadoSubactividad.FINALIZADA)
            .count();

        actividad.setPorcentajeAvance(Math.max(0, Math.min(100, avance)));
        actividad.setEstado(finalizadas == subactividades.size()
            ? EstadoActividad.FINALIZADA
            : subactividades.stream().anyMatch(s -> s.getEstado() != EstadoSubactividad.PENDIENTE)
                ? EstadoActividad.EN_CURSO
                : EstadoActividad.PENDIENTE);
        actividad.setFechaInicioReal(subactividades.stream()
            .map(Subactividad::getFechaInicioReal)
            .filter(Objects::nonNull)
            .min(LocalDate::compareTo)
            .orElse(null));
        actividad.setFechaFinReal(finalizadas == subactividades.size()
            ? subactividades.stream()
                .map(Subactividad::getFechaFinReal)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null)
            : null);
    }

    private int porcentajeSubactividad(Subactividad subactividad) {
        if (subactividad.getEstado() == EstadoSubactividad.FINALIZADA) return 100;
        return subactividad.getPorcentajeAvance() == null
            ? (subactividad.getEstado() == EstadoSubactividad.EN_CURSO ? 50 : 0)
            : subactividad.getPorcentajeAvance();
    }

    public ResumenHito resumir(Hito hito) {
        List<Actividad> actividades =
            actividadRepository.findByHitoIdOrderByFechaInicioPlanificadaAscIdAsc(hito.getId());
        if (actividades.isEmpty()) {
            return new ResumenHito(0D, null, null, 0L, 0, 0, EstadoHito.PENDIENTE);
        }

        actividades.forEach(this::sincronizarActividadDesdeSubactividades);
        long pesoTotal = actividades.stream().mapToLong(this::duracionActividad).sum();
        double sumaPonderada = actividades.stream()
            .mapToDouble(a -> valorAvance(a) * duracionActividad(a))
            .sum();
        LocalDate inicio = actividades.stream()
            .map(Actividad::getFechaInicioPlanificada).filter(Objects::nonNull)
            .min(LocalDate::compareTo).orElse(null);
        LocalDate fin = actividades.stream()
            .map(Actividad::getFechaFinPlanificada).filter(Objects::nonNull)
            .max(LocalDate::compareTo).orElse(null);
        int finalizadas = (int) actividades.stream()
            .filter(a -> a.getEstado() == EstadoActividad.FINALIZADA).count();
        EstadoHito estado = finalizadas == actividades.size()
            ? EstadoHito.FINALIZADO
            : actividades.stream().anyMatch(a -> a.getEstado() != EstadoActividad.PENDIENTE)
                ? EstadoHito.EN_CURSO
                : EstadoHito.PENDIENTE;
        if (estado == EstadoHito.FINALIZADO) {
            hito.setFechaCumplimientoReal(actividades.stream()
                .map(Actividad::getFechaFinReal)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null));
        } else {
            hito.setFechaCumplimientoReal(null);
        }
        hito.setEstado(estado);
        return new ResumenHito(
            pesoTotal == 0 ? 0D : sumaPonderada / pesoTotal,
            inicio,
            fin,
            inicio != null && fin != null ? duracion(inicio, fin) : pesoTotal,
            actividades.size(),
            finalizadas,
            estado
        );
    }

    @Transactional
    public void recalcularFase(Long faseId) {
        Fase fase = faseRepository.findById(faseId).orElse(null);
        if (fase == null) return;
        ResumenFase resumen = resumirFase(fase);
        fase.setPorcentajeAvance(resumen.porcentajeAvance());
        fase.setEstado(resumen.estado());
        fase.setFechaInicioReal(resumen.fechaInicioReal());
        fase.setFechaFinReal(resumen.fechaFinReal());
        faseRepository.save(fase);
    }

    public ResumenFase resumirFase(Fase fase) {
        List<Actividad> actividades =
            actividadRepository.findByFaseIdOrderByFechaInicioPlanificadaAscIdAsc(fase.getId());
        if (actividades.isEmpty()) {
            return new ResumenFase(0D, null, null, 0, 0, EstadoFase.PENDIENTE);
        }
        actividades.forEach(this::sincronizarActividadDesdeSubactividades);
        long pesoTotal = actividades.stream().mapToLong(this::duracionActividad).sum();
        double sumaPonderada = actividades.stream()
            .mapToDouble(a -> valorAvance(a) * duracionActividad(a)).sum();
        int finalizadas = (int) actividades.stream()
            .filter(a -> a.getEstado() == EstadoActividad.FINALIZADA).count();
        EstadoFase estado = finalizadas == actividades.size()
            ? EstadoFase.FINALIZADA
            : actividades.stream().anyMatch(a -> a.getEstado() != EstadoActividad.PENDIENTE)
                ? EstadoFase.EN_CURSO
                : EstadoFase.PENDIENTE;
        LocalDate inicioReal = actividades.stream()
            .map(Actividad::getFechaInicioReal).filter(Objects::nonNull)
            .min(LocalDate::compareTo).orElse(null);
        LocalDate finReal = estado == EstadoFase.FINALIZADA
            ? actividades.stream().map(Actividad::getFechaFinReal).filter(Objects::nonNull)
                .max(LocalDate::compareTo).orElse(null)
            : null;
        return new ResumenFase(
            pesoTotal == 0 ? 0D : sumaPonderada / pesoTotal,
            inicioReal,
            finReal,
            actividades.size(),
            finalizadas,
            estado
        );
    }

    private int valorAvance(Actividad actividad) {
        return actividad.getPorcentajeAvance() == null ? 0 : actividad.getPorcentajeAvance();
    }

    private void recalcularHito(Hito hito) {
        if (hito == null) return;
        resumir(hito);
        hitoRepository.save(hito);
    }

    @Transactional
    public void recalcularProyecto(Long proyectoId) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId).orElse(null);
        if (proyecto == null) return;
        List<Fase> fases = faseRepository.findByProyectoIdOrderByFechaInicioPlanificadaAscIdAsc(proyectoId);
        double sumaPonderada = 0D;
        long pesoTotal = 0L;
        for (Fase fase : fases) {
            ResumenFase resumen = resumirFase(fase);
            fase.setPorcentajeAvance(resumen.porcentajeAvance());
            fase.setEstado(resumen.estado());
            fase.setFechaInicioReal(resumen.fechaInicioReal());
            fase.setFechaFinReal(resumen.fechaFinReal());
            faseRepository.save(fase);
            sumaPonderada += resumen.porcentajeAvance() * duracionFase(fase);
            pesoTotal += duracionFase(fase);
        }
        proyecto.setPorcentajeAvance(pesoTotal == 0 ? 0D : sumaPonderada / pesoTotal);
        proyectoRepository.save(proyecto);
    }

    public record ResumenHito(
        Double porcentajeAvance,
        LocalDate fechaInicioPlanificada,
        LocalDate fechaFinPlanificada,
        Long duracionDias,
        Integer totalActividades,
        Integer actividadesFinalizadas,
        EstadoHito estado
    ) {}

    public record ResumenFase(
        Double porcentajeAvance,
        LocalDate fechaInicioReal,
        LocalDate fechaFinReal,
        Integer totalActividades,
        Integer actividadesFinalizadas,
        EstadoFase estado
    ) {}
}
