package com.redmuqui.platform.actividad.service;

import com.redmuqui.platform.actividad.dto.CofinanciamientoDisponibleDTO;
import com.redmuqui.platform.actividad.dto.CofinanciamientoDisponibleDTO.ActividadDisponibleDTO;
import com.redmuqui.platform.actividad.dto.CofinanciamientoDisponibleDTO.ProyectoDisponibleDTO;
import com.redmuqui.platform.actividad.entity.Actividad;
import com.redmuqui.platform.actividad.repository.ActividadRepository;
import com.redmuqui.platform.actividad.repository.SubactividadCofinanciamientoRepository;
import com.redmuqui.platform.actividad.repository.SubactividadRepository;
import com.redmuqui.platform.common.exception.BusinessException;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CofinanciamientoService {

    private final ActividadRepository actividadRepository;
    private final SubactividadRepository subactividadRepository;
    private final SubactividadCofinanciamientoRepository cofinanciamientoRepository;

    @Transactional(readOnly = true)
    public CofinanciamientoDisponibleDTO listarDisponibles(String moneda, Long excludeProyectoId) {
        String monedaNormalizada = normalizarMoneda(moneda);
        Map<Long, ProyectoDisponibleBuilder> proyectos = new LinkedHashMap<>();

        for (Object[] row : actividadRepository.findCandidatasCofinanciamiento(monedaNormalizada, excludeProyectoId)) {
            Actividad actividad = (Actividad) row[0];
            double presupuesto = valor(actividad.getPresupuesto());
            double presupuestoComprometido = valor(row[1]) + valor(row[2]);
            double presupuestoDisponible = presupuesto - presupuestoComprometido;

            if (presupuestoDisponible <= 0) {
                continue;
            }

            Proyecto proyecto = actividad.getProyecto();
            ProyectoDisponibleBuilder builder = proyectos.computeIfAbsent(
                proyecto.getId(),
                id -> new ProyectoDisponibleBuilder(proyecto.getId(), proyecto.getNombre(), proyecto.getMoneda())
            );
            builder.actividades().add(new ActividadDisponibleDTO(
                actividad.getId(),
                actividad.getNombre(),
                presupuesto,
                presupuestoComprometido,
                presupuestoDisponible
            ));
        }

        List<ProyectoDisponibleDTO> proyectosDisponibles = proyectos.values().stream()
            .map(ProyectoDisponibleBuilder::toDTO)
            .toList();
        return new CofinanciamientoDisponibleDTO(monedaNormalizada, excludeProyectoId, proyectosDisponibles);
    }

    @Transactional(readOnly = true)
    public double calcularPresupuestoDisponible(Actividad actividad) {
        double presupuesto = valor(actividad.getPresupuesto());
        return presupuesto - calcularPresupuestoComprometido(actividad);
    }

    @Transactional(readOnly = true)
    public double calcularPresupuestoComprometido(Actividad actividad) {
        return subactividadRepository.sumPresupuestoByActividadId(actividad.getId())
            + cofinanciamientoRepository.sumMontoByActividadOrigenId(actividad.getId());
    }

    @Transactional(readOnly = true)
    public double calcularPresupuestoDisponibleParaSubactividad(
            Actividad actividad,
            Double presupuestoActualSubactividad) {
        return calcularPresupuestoDisponible(actividad) + valor(presupuestoActualSubactividad);
    }

    private String normalizarMoneda(String moneda) {
        if (moneda == null || moneda.isBlank()) {
            throw new BusinessException("La moneda es obligatoria para consultar disponibles");
        }
        return moneda.trim().toUpperCase(Locale.ROOT);
    }

    private double valor(Double valor) {
        return valor == null ? 0D : valor;
    }

    private double valor(Object valor) {
        return valor instanceof Number number ? number.doubleValue() : 0D;
    }

    private record ProyectoDisponibleBuilder(
        Long proyectoId,
        String proyectoNombre,
        String moneda,
        List<ActividadDisponibleDTO> actividades
    ) {
        ProyectoDisponibleBuilder(Long proyectoId, String proyectoNombre, String moneda) {
            this(proyectoId, proyectoNombre, moneda, new ArrayList<>());
        }

        ProyectoDisponibleDTO toDTO() {
            return new ProyectoDisponibleDTO(proyectoId, proyectoNombre, moneda, actividades);
        }
    }
}
