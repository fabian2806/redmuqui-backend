package com.redmuqui.platform.actividad.service;

import com.redmuqui.platform.actividad.entity.Actividad;
import com.redmuqui.platform.actividad.entity.EstadoActividad;
import com.redmuqui.platform.actividad.entity.EstadoHito;
import com.redmuqui.platform.actividad.entity.EstadoSubactividad;
import com.redmuqui.platform.actividad.entity.Hito;
import com.redmuqui.platform.actividad.entity.Subactividad;
import com.redmuqui.platform.actividad.repository.ActividadRepository;
import com.redmuqui.platform.actividad.repository.HitoRepository;
import com.redmuqui.platform.actividad.repository.SubactividadRepository;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.proyecto.repository.ProyectoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvanceProyectoService {

    private final ActividadRepository actividadRepository;
    private final HitoRepository hitoRepository;
    private final SubactividadRepository subactividadRepository;
    private final ProyectoRepository proyectoRepository;

    public long duracionActividad(Actividad actividad) {
        if (actividad.getFechaInicio() == null || actividad.getFechaFin() == null
                || actividad.getFechaFin().isBefore(actividad.getFechaInicio())) {
            return 1L;
        }
        return ChronoUnit.DAYS.between(actividad.getFechaInicio(), actividad.getFechaFin()) + 1L;
    }

    public long duracionSubactividad(Subactividad subactividad) {
        if (subactividad.getFechaInicio() == null || subactividad.getFechaFin() == null
                || subactividad.getFechaFin().isBefore(subactividad.getFechaInicio())) {
            return 1L;
        }
        return ChronoUnit.DAYS.between(subactividad.getFechaInicio(), subactividad.getFechaFin()) + 1L;
    }

    @Transactional
    public void recalcularActividad(Long actividadId) {
        Actividad actividad = actividadRepository.findById(actividadId).orElse(null);
        if (actividad == null) return;
        sincronizarActividadDesdeSubactividades(actividad);
        actividadRepository.save(actividad);
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
            : subactividades.stream().anyMatch(s -> s.getEstado() == EstadoSubactividad.EN_CURSO || s.getEstado() == EstadoSubactividad.FINALIZADA)
                ? EstadoActividad.EN_CURSO
                : EstadoActividad.PENDIENTE);
    }

    private int porcentajeSubactividad(Subactividad subactividad) {
        return switch (subactividad.getEstado()) {
            case FINALIZADA -> 100;
            case EN_CURSO -> 50;
            case PENDIENTE -> 0;
        };
    }

    public ResumenHito resumir(Hito hito) {
        List<Actividad> actividades = actividadRepository.findByHitoIdOrderByFechaInicioAscIdAsc(hito.getId());
        if (actividades.isEmpty()) {
            return new ResumenHito(0D, null, null, 0L, 0, 0, EstadoHito.PENDIENTE);
        }

        actividades.forEach(this::sincronizarActividadDesdeSubactividades);
        long pesoTotal = actividades.stream().mapToLong(this::duracionActividad).sum();
        double sumaPonderada = actividades.stream()
            .mapToDouble(a -> (a.getPorcentajeAvance() == null ? 0 : a.getPorcentajeAvance()) * duracionActividad(a))
            .sum();
        var inicio = actividades.stream().map(Actividad::getFechaInicio).filter(java.util.Objects::nonNull).min(java.time.LocalDate::compareTo).orElse(null);
        var fin = actividades.stream().map(Actividad::getFechaFin).filter(java.util.Objects::nonNull).max(java.time.LocalDate::compareTo).orElse(null);
        long duracion = inicio != null && fin != null && !fin.isBefore(inicio)
            ? ChronoUnit.DAYS.between(inicio, fin) + 1L
            : pesoTotal;
        int finalizadas = (int) actividades.stream().filter(a -> a.getEstado() == EstadoActividad.FINALIZADA).count();
        double avance = pesoTotal == 0 ? 0D : sumaPonderada / pesoTotal;
        EstadoHito estado = finalizadas == actividades.size()
            ? EstadoHito.FINALIZADO
            : finalizadas > 0 || actividades.stream().anyMatch(a -> a.getEstado() == EstadoActividad.EN_CURSO)
                ? EstadoHito.EN_CURSO
                : EstadoHito.PENDIENTE;
        return new ResumenHito(avance, inicio, fin, Math.max(duracion, 1L), actividades.size(), finalizadas, estado);
    }

    @Transactional
    public void recalcularProyecto(Long proyectoId) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId).orElse(null);
        if (proyecto == null) return;

        List<Hito> hitos = hitoRepository.findByProyectoIdOrderByFechaClaveAscIdAsc(proyectoId);
        double sumaPonderada = 0D;
        long pesoTotal = 0L;
        for (Hito hito : hitos) {
            ResumenHito resumen = resumir(hito);
            sincronizarEstado(hito, resumen);
            if (resumen.totalActividades() > 0) {
                sumaPonderada += resumen.porcentajeAvance() * resumen.duracionDias();
                pesoTotal += resumen.duracionDias();
            }
        }
        proyecto.setPorcentajeAvance(pesoTotal == 0 ? 0D : sumaPonderada / pesoTotal);
        proyectoRepository.save(proyecto);
    }

    private void sincronizarEstado(Hito hito, ResumenHito resumen) {
        hito.setEstado(resumen.estado());
    }

    public record ResumenHito(
        Double porcentajeAvance,
        java.time.LocalDate fechaInicio,
        java.time.LocalDate fechaFin,
        Long duracionDias,
        Integer totalActividades,
        Integer actividadesFinalizadas,
        EstadoHito estado
    ) {}
}
