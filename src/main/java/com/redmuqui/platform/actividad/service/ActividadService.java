package com.redmuqui.platform.actividad.service;

import com.redmuqui.platform.actividad.dto.ActividadCreateDTO;
import com.redmuqui.platform.actividad.dto.ActividadResponseDTO;
import com.redmuqui.platform.actividad.entity.Actividad;
import com.redmuqui.platform.actividad.entity.EstadoActividad;
import com.redmuqui.platform.actividad.repository.ActividadRepository;
import com.redmuqui.platform.common.exception.ResourceNotFoundException;
import com.redmuqui.platform.proyecto.repository.ProyectoRepository;
import com.redmuqui.platform.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.redmuqui.platform.actividad.dto.SubactividadArchivoResponseDTO;
import com.redmuqui.platform.actividad.dto.SubactividadCofinanciamientoResponseDTO;
import com.redmuqui.platform.actividad.dto.SubactividadResponseDTO;
import com.redmuqui.platform.actividad.entity.Subactividad;
import com.redmuqui.platform.proyecto.entity.Proyecto;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActividadService {

    private final ActividadRepository actividadRepository;
    private final ProyectoRepository proyectoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Page<ActividadResponseDTO> listar(Pageable pageable) {
        return actividadRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public ActividadResponseDTO obtener(Long id) {
        return toDTO(buscarOFallar(id));
    }

    @Transactional(readOnly = true)
    public List<ActividadResponseDTO> listarPorProyecto(Long proyectoId) {
        return actividadRepository.findByProyectoId(proyectoId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ActividadResponseDTO crear(ActividadCreateDTO dto) {
        Actividad actividad = Actividad.builder()
            .nombre(dto.nombre())
            .descripcion(dto.descripcion())
            .fechaInicio(dto.fechaInicio())
            .fechaFin(dto.fechaFin())
            .estado(dto.estado() != null ? dto.estado() : EstadoActividad.PENDIENTE)
            .proyecto(proyectoRepository.findById(dto.idProyecto())
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto", dto.idProyecto())))
            .build();

        if (dto.idResponsables() != null && !dto.idResponsables().isEmpty()) {
            actividad.setResponsables(new HashSet<>(usuarioRepository.findAllById(dto.idResponsables())));
        }

        return toDTO(actividadRepository.save(actividad));
    }

    @Transactional
    public ActividadResponseDTO cambiarEstado(Long id, EstadoActividad nuevoEstado) {
        Actividad a = buscarOFallar(id);
        a.setEstado(nuevoEstado);
        return toDTO(a);
    }

    @Transactional
    public ActividadResponseDTO actualizarAvance(Long id, Integer porcentajeAvance) {
        Actividad a = buscarOFallar(id);
        a.setPorcentajeAvance(porcentajeAvance);
        a = actividadRepository.save(a);
        
        // Calcular nuevo avance del proyecto
        Proyecto p = a.getProyecto();
        List<Actividad> actividades = actividadRepository.findByProyectoId(p.getId());
        if (!actividades.isEmpty()) {
            int total = actividades.stream().mapToInt(act -> act.getPorcentajeAvance() != null ? act.getPorcentajeAvance() : 0).sum();
            p.setPorcentajeAvance((double) total / actividades.size());
            proyectoRepository.save(p);
        }
        
        return toDTO(a);
    }

    private Actividad buscarOFallar(Long id) {
        return actividadRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Actividad", id));
    }

    private ActividadResponseDTO toDTO(Actividad a) {
        return new ActividadResponseDTO(
            a.getId(), a.getNombre(), a.getDescripcion(),
            a.getFechaInicio(), a.getFechaFin(), a.getEstado(),
            a.getPorcentajeAvance(),
            a.getProyecto().getId(),
            a.getResponsables().stream().map(u -> u.getId()).collect(Collectors.toSet()),
            a.getSubactividades() == null ? List.of() : a.getSubactividades().stream()
                .map(this::mapSubactividad)
                .collect(Collectors.toList())
        );
    }

    private SubactividadResponseDTO mapSubactividad(Subactividad s) {
        return new SubactividadResponseDTO(
            s.getId(),
            s.getNombre(),
            s.getResponsable().getNombres() + " " + s.getResponsable().getApellidos(),
            s.getPresupuesto(),
            s.getHombresInvolucrados(),
            s.getMujeresInvolucradas(),
            s.getFechaInicio(),
            s.getFechaFin(),
            s.getDescripcion(),
            s.getArchivosEvidencia() == null ? List.of() : s.getArchivosEvidencia().stream()
                .map(a -> new SubactividadArchivoResponseDTO(a.getId(), a.getNombre(), a.getUrl()))
                .collect(Collectors.toList()),
            s.getCofinanciamientos() == null ? List.of() : s.getCofinanciamientos().stream()
                .map(c -> new SubactividadCofinanciamientoResponseDTO(c.getActividadOrigen().getId(), c.getMonto()))
                .collect(Collectors.toList())
        );
    }
}
