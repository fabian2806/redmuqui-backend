package com.redmuqui.platform.reporte.service;

import com.redmuqui.platform.actividad.entity.Actividad;
import com.redmuqui.platform.actividad.entity.EstadoActividad;
import com.redmuqui.platform.actividad.repository.ActividadRepository;
import com.redmuqui.platform.actividad.repository.HitoRepository;
import com.redmuqui.platform.actividad.repository.SubactividadRepository;
import com.redmuqui.platform.documento.entity.EstadoDocumento;
import com.redmuqui.platform.documento.entity.Documento;
import com.redmuqui.platform.documento.repository.DocumentoRepository;
import com.redmuqui.platform.proyecto.entity.EstadoProyecto;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.proyecto.repository.ProyectoRepository;
import com.redmuqui.platform.reporte.dto.ActividadRecienteDTO;
import com.redmuqui.platform.reporte.dto.CoberturaTerritorialDTO;
import com.redmuqui.platform.reporte.dto.ConteoDTO;
import com.redmuqui.platform.reporte.dto.ConteoPresupuestoDTO;
import com.redmuqui.platform.reporte.dto.DocumentoRecienteDTO;
import com.redmuqui.platform.reporte.dto.IndicadoresDTO;
import com.redmuqui.platform.reporte.dto.MacroregionResumenDTO;
import com.redmuqui.platform.reporte.dto.PresupuestoPorMonedaDTO;
import com.redmuqui.platform.reporte.dto.ProyectoAvanceDTO;
import com.redmuqui.platform.reporte.dto.ProyectoRiesgoDTO;
import com.redmuqui.platform.territorio.entity.TipoTerritorio;
import com.redmuqui.platform.territorio.repository.TerritorioRepository;
import com.redmuqui.platform.trazabilidad.repository.BitacoraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
    private final TerritorioRepository territorioRepository;
    private final BitacoraRepository bitacoraRepository;

    @Transactional(readOnly = true)
    public IndicadoresDTO obtenerIndicadores() {
        return obtenerIndicadores(null);
    }

    @Transactional(readOnly = true)
    public IndicadoresDTO obtenerIndicadores(Integer anio) {
        if (anio == null) {
            return new IndicadoresDTO(
                proyectoRepository.countByEstado(EstadoProyecto.ACTIVO),
                proyectosEnRiesgo().size(),
                proyectoRepository.sumPresupuestoByEstado(EstadoProyecto.ACTIVO),
                presupuestosPorMonedaActivos(),
                proyectoRepository.avgAvanceByEstado(EstadoProyecto.ACTIVO),
                subactividadRepository.sumHombresInvolucrados(),
                subactividadRepository.sumMujeresInvolucradas(),
                documentoRepository.countByEstado(EstadoDocumento.PUBLICADO),
                documentoRepository.countByEstadoIn(List.of(EstadoDocumento.BORRADOR, EstadoDocumento.EN_REVISION))
            );
        }

        List<Proyecto> proyectosActivos = proyectosDelAnio(anio).stream()
            .filter(p -> p.getEstado() == EstadoProyecto.ACTIVO)
            .toList();
        List<Documento> documentos = documentosDelAnio(anio);
        double presupuesto = proyectosActivos.stream()
            .mapToDouble(p -> p.getPresupuesto() == null ? 0.0 : p.getPresupuesto())
            .sum();
        double avancePromedio = proyectosActivos.stream()
            .mapToDouble(p -> p.getPorcentajeAvance() == null ? 0.0 : p.getPorcentajeAvance())
            .average()
            .orElse(0.0);

        return new IndicadoresDTO(
            proyectosActivos.size(),
            proyectosEnRiesgo(anio).size(),
            presupuesto,
            presupuestosPorMoneda(proyectosActivos),
            avancePromedio,
            subactividadRepository.sumHombresInvolucrados(),
            subactividadRepository.sumMujeresInvolucradas(),
            documentos.stream().filter(d -> d.getEstado() == EstadoDocumento.PUBLICADO).count(),
            documentos.stream()
                .filter(d -> d.getEstado() == EstadoDocumento.BORRADOR || d.getEstado() == EstadoDocumento.EN_REVISION)
                .count()
        );
    }

    private List<PresupuestoPorMonedaDTO> presupuestosPorMonedaActivos() {
        Map<String, PresupuestoMonedaAccumulator> resumen = new LinkedHashMap<>();
        for (Object[] fila : proyectoRepository.sumPresupuestoPorMonedaByEstado(EstadoProyecto.ACTIVO)) {
            String moneda = normalizarMoneda((String) fila[0]);
            PresupuestoMonedaAccumulator acc = resumen.computeIfAbsent(
                moneda,
                key -> new PresupuestoMonedaAccumulator()
            );
            acc.monto += ((Number) fila[1]).doubleValue();
            acc.proyectos += ((Number) fila[2]).longValue();
        }
        return resumen.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> new PresupuestoPorMonedaDTO(
                entry.getKey(),
                entry.getValue().monto,
                entry.getValue().proyectos
            ))
            .toList();
    }

    private List<PresupuestoPorMonedaDTO> presupuestosPorMoneda(List<Proyecto> proyectos) {
        Map<String, PresupuestoMonedaAccumulator> resumen = new LinkedHashMap<>();
        for (Proyecto proyecto : proyectos) {
            String moneda = normalizarMoneda(proyecto.getMoneda());
            PresupuestoMonedaAccumulator acc = resumen.computeIfAbsent(
                moneda,
                key -> new PresupuestoMonedaAccumulator()
            );
            acc.monto += proyecto.getPresupuesto() == null ? 0.0 : proyecto.getPresupuesto();
            acc.proyectos++;
        }
        return resumen.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> new PresupuestoPorMonedaDTO(
                entry.getKey(),
                entry.getValue().monto,
                entry.getValue().proyectos
            ))
            .toList();
    }

    private String normalizarMoneda(String moneda) {
        return moneda == null || moneda.isBlank() ? "PEN" : moneda.trim().toUpperCase(Locale.ROOT);
    }

    @Transactional(readOnly = true)
    public List<ConteoDTO> proyectosPorMacroregion() {
        return proyectosPorMacroregion(null);
    }

    @Transactional(readOnly = true)
    public List<ConteoDTO> proyectosPorMacroregion(Integer anio) {
        if (anio == null) {
            return proyectoRepository.contarPorMacroregion().stream()
                .map(fila -> new ConteoDTO((String) fila[0], ((Number) fila[1]).longValue()))
                .toList();
        }

        Map<String, Long> conteos = new HashMap<>();
        for (Proyecto proyecto : proyectosDelAnio(anio)) {
            proyecto.getMacroregiones().forEach(m -> conteos.merge(m.getNombre(), 1L, Long::sum));
        }

        return conteos.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .map(entry -> new ConteoDTO(entry.getKey(), entry.getValue()))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ConteoDTO> proyectosPorEstado() {
        return proyectosPorEstado(null);
    }

    @Transactional(readOnly = true)
    public List<ConteoDTO> proyectosPorEstado(Integer anio) {
        Map<EstadoProyecto, Long> conteos = new HashMap<>();
        for (Proyecto proyecto : proyectosDelAnio(anio)) {
            conteos.merge(proyecto.getEstado(), 1L, Long::sum);
        }

        return List.of(
            new ConteoDTO("En ejecucion", conteos.getOrDefault(EstadoProyecto.ACTIVO, 0L)),
            new ConteoDTO("Finalizados", conteos.getOrDefault(EstadoProyecto.CERRADO, 0L)),
            new ConteoDTO("Suspendidos", conteos.getOrDefault(EstadoProyecto.SUSPENDIDO, 0L))
        );
    }

    @Transactional(readOnly = true)
    public List<ConteoPresupuestoDTO> proyectosPorEjeTematico() {
        return proyectosPorEjeTematico(null);
    }

    @Transactional(readOnly = true)
    public List<ConteoPresupuestoDTO> proyectosPorEjeTematico(Integer anio) {
        Map<String, ConteoPresupuestoAccumulator> resumen = new HashMap<>();
        for (Proyecto proyecto : proyectosDelAnio(anio)) {
            String eje = proyecto.getEjeTematico() == null
                ? "Sin eje tematico"
                : proyecto.getEjeTematico().getNombre();
            ConteoPresupuestoAccumulator acc = resumen.computeIfAbsent(eje, key -> new ConteoPresupuestoAccumulator());
            acc.cantidad++;
            acc.presupuesto += proyecto.getPresupuesto() == null ? 0.0 : proyecto.getPresupuesto();
        }

        return resumen.entrySet().stream()
            .sorted(Map.Entry.<String, ConteoPresupuestoAccumulator>comparingByValue(
                Comparator.comparingLong(acc -> acc.cantidad)
            ).reversed())
            .map(entry -> new ConteoPresupuestoDTO(
                entry.getKey(),
                entry.getValue().cantidad,
                entry.getValue().presupuesto
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ProyectoAvanceDTO> avanceProyectos() {
        return avanceProyectos(null);
    }

    @Transactional(readOnly = true)
    public List<ProyectoAvanceDTO> avanceProyectos(Integer anio) {
        return proyectosDelAnio(anio).stream()
            .sorted(Comparator.comparingDouble(p -> p.getPorcentajeAvance() == null ? 0.0 : p.getPorcentajeAvance()))
            .map(p -> new ProyectoAvanceDTO(
                p.getId(),
                p.getNombre(),
                p.getMacroregiones().stream()
                    .map(m -> m.getNombre())
                    .sorted()
                    .findFirst()
                    .orElse("Sin macroregion"),
                p.getEstado(),
                p.getPorcentajeAvance() == null ? 0.0 : p.getPorcentajeAvance()
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ConteoDTO> actividadesPorEstado() {
        return actividadesPorEstado(null);
    }

    @Transactional(readOnly = true)
    public List<ConteoDTO> actividadesPorEstado(Integer anio) {
        LocalDate hoy = LocalDate.now();
        if (anio != null) {
            List<Actividad> actividades = actividadRepository.findAll().stream()
                .filter(a -> enAnio(a.getFechaInicioPlanificada(), anio) || enAnio(a.getFechaFinPlanificada(), anio))
                .toList();
            return List.of(
                new ConteoDTO("Finalizadas", actividades.stream().filter(a -> a.getEstado() == EstadoActividad.FINALIZADA).count()),
                new ConteoDTO("En curso", actividades.stream()
                    .filter(a -> a.getEstado() == EstadoActividad.EN_CURSO)
                    .filter(a -> a.getFechaFinReal() == null || !a.getFechaFinReal().isBefore(hoy))
                    .count()),
                new ConteoDTO("Pendientes", actividades.stream()
                    .filter(a -> a.getEstado() == EstadoActividad.PENDIENTE)
                    .filter(a -> a.getFechaFinReal() == null || !a.getFechaFinReal().isBefore(hoy))
                    .count()),
                new ConteoDTO("Vencidas", actividades.stream()
                    .filter(a -> a.getFechaFinReal() != null && a.getFechaFinReal().isBefore(hoy))
                    .filter(a -> a.getEstado() != EstadoActividad.FINALIZADA)
                    .count())
            );
        }

        return List.of(
            new ConteoDTO("Finalizadas", actividadRepository.countByEstado(EstadoActividad.FINALIZADA)),
            new ConteoDTO("En curso", actividadRepository.countVigentesByEstado(EstadoActividad.EN_CURSO, hoy)),
            new ConteoDTO("Pendientes", actividadRepository.countVigentesByEstado(EstadoActividad.PENDIENTE, hoy)),
            new ConteoDTO("Vencidas", actividadRepository.countVencidas(hoy))
        );
    }

    @Transactional(readOnly = true)
    public List<ProyectoRiesgoDTO> proyectosEnRiesgo() {
        return proyectosEnRiesgo(null);
    }

    @Transactional(readOnly = true)
    public List<ProyectoRiesgoDTO> proyectosEnRiesgo(Integer anio) {
        LocalDate hoy = LocalDate.now();

        Map<Long, Long> hitosVencidosPorProyecto = new HashMap<>();
        for (Object[] fila : hitoRepository.contarHitosVencidosPorProyecto(hoy)) {
            hitosVencidosPorProyecto.put(((Number) fila[0]).longValue(), ((Number) fila[1]).longValue());
        }

        List<Proyecto> proyectosActivos = anio == null
            ? proyectoRepository.findByEstado(EstadoProyecto.ACTIVO)
            : proyectosDelAnio(anio).stream()
                .filter(p -> p.getEstado() == EstadoProyecto.ACTIVO)
                .toList();

        return proyectosActivos.stream()
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
        return documentosRecientes(null);
    }

    @Transactional(readOnly = true)
    public List<DocumentoRecienteDTO> documentosRecientes(Integer anio) {
        List<Documento> documentos = anio == null
            ? documentoRepository.findTop5ByOrderByFechaCreacionDescIdDesc()
            : documentosDelAnio(anio);

        return documentos.stream()
            .sorted(Comparator
                .comparing(Documento::getFechaCarga, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Documento::getId, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(5)
            .map(d -> new DocumentoRecienteDTO(
                d.getId(),
                d.getTitulo(),
                d.getTipo(),
                d.getEstado(),
                d.getFechaCarga()
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ConteoDTO> documentosPorTipo() {
        return documentosPorTipo(null);
    }

    @Transactional(readOnly = true)
    public List<ConteoDTO> documentosPorTipo(Integer anio) {
        Map<String, Long> conteos = new HashMap<>();
        documentosDelAnio(anio).forEach(d ->
            conteos.merge(d.getTipo() == null ? "Sin tipo" : d.getTipo(), 1L, Long::sum)
        );

        return conteos.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .map(entry -> new ConteoDTO(entry.getKey(), entry.getValue()))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ConteoDTO> documentosPorEstado() {
        return documentosPorEstado(null);
    }

    @Transactional(readOnly = true)
    public List<ConteoDTO> documentosPorEstado(Integer anio) {
        Map<EstadoDocumento, Long> conteos = new HashMap<>();
        documentosDelAnio(anio).forEach(d -> conteos.merge(d.getEstado(), 1L, Long::sum));

        return List.of(
            new ConteoDTO("Borrador", conteos.getOrDefault(EstadoDocumento.BORRADOR, 0L)),
            new ConteoDTO("En revision", conteos.getOrDefault(EstadoDocumento.EN_REVISION, 0L)),
            new ConteoDTO("Publicados", conteos.getOrDefault(EstadoDocumento.PUBLICADO, 0L))
        );
    }

    @Transactional(readOnly = true)
    public List<MacroregionResumenDTO> resumenMacroregiones() {
        return resumenMacroregiones(null);
    }

    @Transactional(readOnly = true)
    public List<MacroregionResumenDTO> resumenMacroregiones(Integer anio) {
        Map<String, MacroregionAccumulator> resumen = new LinkedHashMap<>();

        for (Proyecto proyecto : proyectosDelAnio(anio)) {
            List<String> macroregiones = proyecto.getMacroregiones().stream()
                .map(m -> m.getNombre())
                .sorted()
                .toList();
            if (macroregiones.isEmpty()) {
                macroregiones = List.of("Sin macroregion");
            }

            for (String nombre : macroregiones) {
                MacroregionAccumulator acc = resumen.computeIfAbsent(nombre, key -> new MacroregionAccumulator());
                acc.totalProyectos++;
                if (proyecto.getEstado() == EstadoProyecto.ACTIVO) acc.activos++;
                if (proyecto.getEstado() == EstadoProyecto.CERRADO) acc.finalizados++;
                proyecto.getInstituciones().forEach(pi -> acc.instituciones.put(pi.getInstitucion().getId(), true));
            }
        }

        return resumen.entrySet().stream()
            .map(entry -> new MacroregionResumenDTO(
                entry.getKey(),
                entry.getValue().totalProyectos,
                entry.getValue().activos,
                entry.getValue().finalizados,
                entry.getValue().instituciones.size()
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ActividadRecienteDTO> actividadReciente() {
        return actividadReciente(null);
    }

    @Transactional(readOnly = true)
    public List<ActividadRecienteDTO> actividadReciente(Integer anio) {
        return bitacoraRepository.findAllByOrderByFechaDesc(PageRequest.of(0, 200)).getContent().stream()
            .filter(b -> enAnio(b.getFecha(), anio))
            .limit(10)
            .map(b -> new ActividadRecienteDTO(
                b.getUsuario() == null ? "Sistema" : b.getUsuario().getNombres() + " " + b.getUsuario().getApellidos(),
                b.getDescripcion(),
                b.getTipoAccion(),
                b.getEntidadReferenciada(),
                b.getFecha()
            ))
            .toList();
    }

    /**
     * Cobertura de la red por unidad territorial del nivel pedido (hoy: departamento),
     * para el Mapa Territorial (Sprint 4 ④). Parte de TODOS los territorios del nivel
     * y rellena en cero los que no tienen actividad, para que el mapa no deje huecos.
     * Las cifras consideran proyectos de cualquier estado (huella de cobertura).
     */
    @Transactional(readOnly = true)
    public List<CoberturaTerritorialDTO> coberturaTerritorial(TipoTerritorio nivel) {
        Map<Long, Long> proyectosPorTerritorio = new HashMap<>();
        Map<Long, Double> presupuestoPorTerritorio = new HashMap<>();
        for (Object[] fila : proyectoRepository.agregarPorTerritorio()) {
            Long idTerritorio = ((Number) fila[0]).longValue();
            proyectosPorTerritorio.put(idTerritorio, ((Number) fila[1]).longValue());
            presupuestoPorTerritorio.put(idTerritorio, ((Number) fila[2]).doubleValue());
        }

        Map<Long, Long> institucionesPorTerritorio = new HashMap<>();
        for (Object[] fila : proyectoRepository.contarInstitucionesPorTerritorio()) {
            institucionesPorTerritorio.put(((Number) fila[0]).longValue(), ((Number) fila[1]).longValue());
        }

        Map<Long, Long> hombresPorTerritorio = new HashMap<>();
        Map<Long, Long> mujeresPorTerritorio = new HashMap<>();
        for (Object[] fila : subactividadRepository.beneficiariosPorTerritorio()) {
            Long idTerritorio = ((Number) fila[0]).longValue();
            hombresPorTerritorio.put(idTerritorio, ((Number) fila[1]).longValue());
            mujeresPorTerritorio.put(idTerritorio, ((Number) fila[2]).longValue());
        }

        return territorioRepository.findByTipoAndCodigoNotNullOrderByNombreAsc(nivel).stream()
            .map(t -> {
                long hombres = hombresPorTerritorio.getOrDefault(t.getId(), 0L);
                long mujeres = mujeresPorTerritorio.getOrDefault(t.getId(), 0L);
                return new CoberturaTerritorialDTO(
                    t.getId(),
                    t.getCodigo(),
                    t.getNombre(),
                    t.getTipo().name(),
                    proyectosPorTerritorio.getOrDefault(t.getId(), 0L),
                    presupuestoPorTerritorio.getOrDefault(t.getId(), 0.0),
                    hombres + mujeres,
                    hombres,
                    mujeres,
                    institucionesPorTerritorio.getOrDefault(t.getId(), 0L)
                );
            })
            .toList();
    }

    private static class MacroregionAccumulator {
        long totalProyectos;
        long activos;
        long finalizados;
        Map<Long, Boolean> instituciones = new HashMap<>();
    }

    private static class ConteoPresupuestoAccumulator {
        long cantidad;
        double presupuesto;
    }

    private static class PresupuestoMonedaAccumulator {
        double monto;
        long proyectos;
    }

    private List<Proyecto> proyectosDelAnio(Integer anio) {
        return proyectoRepository.findAll().stream()
            .filter(p -> enAnio(p.getFechaInicio(), anio))
            .toList();
    }

    private List<Documento> documentosDelAnio(Integer anio) {
        return documentoRepository.findAll().stream()
            .filter(d -> enAnio(d.getFechaCarga(), anio))
            .toList();
    }

    private boolean enAnio(LocalDate fecha, Integer anio) {
        return anio == null || (fecha != null && fecha.getYear() == anio);
    }

    private boolean enAnio(LocalDateTime fecha, Integer anio) {
        return anio == null || (fecha != null && fecha.getYear() == anio);
    }
}
