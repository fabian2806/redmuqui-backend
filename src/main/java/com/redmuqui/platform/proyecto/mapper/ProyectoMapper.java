package com.redmuqui.platform.proyecto.mapper;

import com.redmuqui.platform.proyecto.dto.ProyectoResponseDTO;
import com.redmuqui.platform.proyecto.dto.ProyectoSummaryDTO;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.usuario.dto.UsuarioSummaryDTO;
import com.redmuqui.platform.actividad.repository.ActividadRepository;
import com.redmuqui.platform.actividad.repository.SubactividadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProyectoMapper {

    private final ActividadRepository actividadRepository;
    private final SubactividadRepository subactividadRepository;

    public ProyectoResponseDTO toResponseDTO(Proyecto p) {
        var macroregiones = p.getMacroregiones().stream()
            .sorted(Comparator.comparing(m -> m.getId()))
            .map(m -> new ProyectoResponseDTO.MacroregionRefDTO(m.getId(), m.getNombre()))
            .collect(Collectors.toCollection(LinkedHashSet::new));
        var macroregionPrincipal = macroregiones.stream().findFirst().orElse(null);

        double costoEstimado = actividadRepository.sumPresupuestoByProyectoId(p.getId());
        double costoReal = subactividadRepository.sumCostoRealByProyectoId(p.getId());
        double porcentajeComprometido = p.getPresupuesto() == null || p.getPresupuesto() <= 0
            ? 0D
            : costoEstimado * 100D / p.getPresupuesto();

        return new ProyectoResponseDTO(
            p.getId(),
            p.getNombre(),
            p.getCodigoInterno(),
            p.getDescripcion(),
            p.getObjetivoGeneral(),
            p.getFechaInicio(),
            p.getFechaFinEstimada(),
            p.getEstado(),
            p.getNivelPrioridad(),
            p.getPorcentajeAvance(),
            calcularAvancePlanificado(p.getFechaInicio(), p.getFechaFinEstimada()),
            p.getPresupuesto(),
            p.getMoneda(),
            costoEstimado,
            costoReal,
            porcentajeComprometido,
            resolverAlertaPresupuesto(porcentajeComprometido),
            macroregionPrincipal != null ? macroregionPrincipal.nombre() : null,
            macroregionPrincipal != null ? macroregionPrincipal.id() : null,
            macroregiones,
            p.getEjeTematico() != null ? p.getEjeTematico().getNombre() : null,
            p.getEjeTematico() != null ? p.getEjeTematico().getId() : null,
            p.getResponsablePrincipal() != null
                ? new UsuarioSummaryDTO(
                    p.getResponsablePrincipal().getId(),
                    p.getResponsablePrincipal().getNombres(),
                    p.getResponsablePrincipal().getApellidos(),
                    p.getResponsablePrincipal().getEmail())
                : null,
            p.getTerritorios().stream()
                .sorted(Comparator.comparing(t -> t.getId()))
                .map(t -> new ProyectoResponseDTO.TerritorioRefDTO(t.getId(), t.getNombre()))
                .collect(Collectors.toCollection(LinkedHashSet::new)),
            p.getInstituciones().stream()
                .sorted(Comparator.comparing(pi -> pi.getInstitucion().getId()))
                .map(pi -> new ProyectoResponseDTO.InstitucionRefDTO(
                    pi.getInstitucion().getId(),
                    pi.getInstitucion().getNombre(),
                    pi.getTipoParticipacion()))
                .collect(Collectors.toCollection(LinkedHashSet::new))
        );
    }

    private double calcularAvancePlanificado(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null) return 0D;
        LocalDate hoy = LocalDate.now();
        if (hoy.isBefore(inicio)) return 0D;
        if (!hoy.isBefore(fin)) return 100D;
        long total = Math.max(1, ChronoUnit.DAYS.between(inicio, fin));
        long transcurrido = ChronoUnit.DAYS.between(inicio, hoy);
        return Math.max(0D, Math.min(100D, transcurrido * 100D / total));
    }

    private String resolverAlertaPresupuesto(double porcentaje) {
        if (porcentaje >= 100D) return "EXCEDIDO";
        if (porcentaje >= 90D) return "CRITICO";
        if (porcentaje >= 80D) return "PREVENTIVO";
        return "NORMAL";
    }

    public ProyectoSummaryDTO toSummaryDTO(Proyecto p) {
        return new ProyectoSummaryDTO(p.getId(), p.getNombre(), p.getCodigoInterno(), p.getEstado(), p.getPorcentajeAvance());
    }
}
