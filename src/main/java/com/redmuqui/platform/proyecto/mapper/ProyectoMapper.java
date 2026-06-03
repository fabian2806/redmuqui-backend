package com.redmuqui.platform.proyecto.mapper;

import com.redmuqui.platform.proyecto.dto.ProyectoResponseDTO;
import com.redmuqui.platform.proyecto.dto.ProyectoSummaryDTO;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.usuario.dto.UsuarioSummaryDTO;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Component
public class ProyectoMapper {

    public ProyectoResponseDTO toResponseDTO(Proyecto p) {
        var macroregiones = p.getMacroregiones().stream()
            .sorted(Comparator.comparing(m -> m.getId()))
            .map(m -> new ProyectoResponseDTO.MacroregionRefDTO(m.getId(), m.getNombre()))
            .collect(Collectors.toCollection(LinkedHashSet::new));
        var macroregionPrincipal = macroregiones.stream().findFirst().orElse(null);

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
            p.getPresupuesto(),
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

    public ProyectoSummaryDTO toSummaryDTO(Proyecto p) {
        return new ProyectoSummaryDTO(p.getId(), p.getNombre(), p.getCodigoInterno(), p.getEstado(), p.getPorcentajeAvance());
    }
}
