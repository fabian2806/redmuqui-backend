package com.redmuqui.platform.proyecto.mapper;

import com.redmuqui.platform.proyecto.dto.ProyectoResponseDTO;
import com.redmuqui.platform.proyecto.dto.ProyectoSummaryDTO;
import com.redmuqui.platform.proyecto.entity.Proyecto;
import com.redmuqui.platform.usuario.dto.UsuarioSummaryDTO;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ProyectoMapper {

    public ProyectoResponseDTO toResponseDTO(Proyecto p) {
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
            p.getMacroregion() != null ? p.getMacroregion().getNombre() : null,
            p.getMacroregion() != null ? p.getMacroregion().getId() : null,
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
                .map(t -> new ProyectoResponseDTO.TerritorioRefDTO(t.getId(), t.getNombre()))
                .collect(Collectors.toSet())
        );
    }

    public ProyectoSummaryDTO toSummaryDTO(Proyecto p) {
        return new ProyectoSummaryDTO(p.getId(), p.getNombre(), p.getCodigoInterno(), p.getEstado(), p.getPorcentajeAvance());
    }
}
