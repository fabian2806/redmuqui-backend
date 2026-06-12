package com.redmuqui.platform.reporte.service;

import com.redmuqui.platform.actividad.entity.EstadoActividad;
import com.redmuqui.platform.actividad.repository.ActividadRepository;
import com.redmuqui.platform.actividad.repository.HitoRepository;
import com.redmuqui.platform.actividad.repository.SubactividadRepository;
import com.redmuqui.platform.documento.entity.EstadoDocumento;
import com.redmuqui.platform.documento.repository.DocumentoRepository;
import com.redmuqui.platform.proyecto.entity.EstadoProyecto;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.proyecto.repository.ProyectoRepository;
import com.redmuqui.platform.reporte.dto.ConteoDTO;
import com.redmuqui.platform.reporte.dto.DocumentoRecienteDTO;
import com.redmuqui.platform.reporte.dto.IndicadoresDTO;
import com.redmuqui.platform.reporte.dto.ProyectoRiesgoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agregaciones del dashboard e indicadores (RF-069 a RF-074).
 *
 * Calcula en servidor las cifras consolidadas de la red a partir de los datos
 * que ya capturan los módulos de proyectos, actividades y documentos.
 */
@Service
@RequiredArgsConstructor
public class ReporteService {

    /** Un proyecto activo a ≤ este número de días de su fin estimado y por debajo del
     *  umbral de avance se considera en riesgo (además de cualquier hito vencido). */
    static final long UMBRAL_DIAS_RIESGO = 30L;
    static final double UMBRAL_AVANCE_RIESGO = 70.0;

    private final ProyectoRepository proyectoRepository;
    private final ActividadRepository actividadRepository;
    private final SubactividadRepository subactividadRepository;
    private final DocumentoRepository documentoRepository;
    private final HitoRepository hitoRepository;

    @Transactional(readOnly = true)
    public IndicadoresDTO obtenerIndicadores() {
        return new IndicadoresDTO(
            proyectoRepository.countByEstado(EstadoProyecto.ACTIVO),
            proyectosEnRiesgo().size(),
            proyectoRepository.sumPresupuestoByEstado(EstadoProyecto.ACTIVO),
            proyectoRepository.avgAvanceByEstado(EstadoProyecto.ACTIVO),
            subactividadRepository.sumHombresInvolucrados(),
            subactividadRepository.sumMujeresInvolucradas(),
            documentoRepository.countByEstado(EstadoDocumento.PUBLICADO),
            documentoRepository.countByEstadoIn(List.of(EstadoDocumento.BORRADOR, EstadoDocumento.EN_REVISION))
        );
    }

    @Transactional(readOnly = true)
    public List<ConteoDTO> proyectosPorMacroregion() {
        return proyectoRepository.contarPorMacroregion().stream()
            .map(fila -> new ConteoDTO((String) fila[0], ((Number) fila[1]).longValue()))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ConteoDTO> actividadesPorEstado() {
        LocalDate hoy = LocalDate.now();
        return List.of(
            new ConteoDTO("Finalizadas", actividadRepository.countByEstado(EstadoActividad.FINALIZADA)),
            new ConteoDTO("En curso", actividadRepository.countVigentesByEstado(EstadoActividad.EN_CURSO, hoy)),
            new ConteoDTO("Pendientes", actividadRepository.countVigentesByEstado(EstadoActividad.PENDIENTE, hoy)),
            new ConteoDTO("Vencidas", actividadRepository.countVencidas(hoy))
        );
    }

    @Transactional(readOnly = true)
    public List<ProyectoRiesgoDTO> proyectosEnRiesgo() {
        LocalDate hoy = LocalDate.now();

        Map<Long, Long> hitosVencidosPorProyecto = new HashMap<>();
        for (Object[] fila : hitoRepository.contarHitosVencidosPorProyecto(hoy)) {
            hitosVencidosPorProyecto.put(((Number) fila[0]).longValue(), ((Number) fila[1]).longValue());
        }

        return proyectoRepository.findByEstado(EstadoProyecto.ACTIVO).stream()
            .map(p -> {
                long hitosVencidos = hitosVencidosPorProyecto.getOrDefault(p.getId(), 0L);
                Long diasRestantes = p.getFechaFinEstimada() == null
                    ? null
                    : ChronoUnit.DAYS.between(hoy, p.getFechaFinEstimada());
                return new ProyectoRiesgoDTO(
                    p.getId(),
                    p.getNombre(),
                    p.getCodigoInterno(),
                    p.getPorcentajeAvance() == null ? 0.0 : p.getPorcentajeAvance(),
                    p.getFechaFinEstimada(),
                    diasRestantes,
                    hitosVencidos
                );
            })
            .filter(this::estaEnRiesgo)
            .sorted(Comparator
                .comparingLong(ProyectoRiesgoDTO::hitosVencidos).reversed()
                .thenComparing(dto -> dto.diasRestantes() == null ? Long.MAX_VALUE : dto.diasRestantes()))
            .toList();
    }

    private boolean estaEnRiesgo(ProyectoRiesgoDTO dto) {
        if (dto.hitosVencidos() > 0) {
            return true;
        }
        return dto.diasRestantes() != null
            && dto.diasRestantes() <= UMBRAL_DIAS_RIESGO
            && dto.porcentajeAvance() < UMBRAL_AVANCE_RIESGO;
    }

    @Transactional(readOnly = true)
    public List<DocumentoRecienteDTO> documentosRecientes() {
        return documentoRepository.findTop5ByOrderByFechaCreacionDescIdDesc().stream()
            .map(d -> new DocumentoRecienteDTO(
                d.getId(),
                d.getTitulo(),
                d.getTipo(),
                d.getEstado(),
                d.getFechaCarga()
            ))
            .toList();
    }
}
