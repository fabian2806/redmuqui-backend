package com.redmuqui.platform.actividad.service;

import com.redmuqui.platform.actividad.dto.ActividadCreateDTO;
import com.redmuqui.platform.actividad.dto.ActividadResponseDTO;
import com.redmuqui.platform.actividad.dto.ActividadUpdateDTO;
import com.redmuqui.platform.actividad.entity.Actividad;
import com.redmuqui.platform.actividad.entity.EstadoActividad;
import com.redmuqui.platform.actividad.entity.Hito;
import com.redmuqui.platform.actividad.repository.ActividadRepository;
import com.redmuqui.platform.actividad.repository.HitoRepository;
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
    private final HitoRepository hitoRepository;
    private final AvanceProyectoService avanceProyectoService;

    @Transactional(readOnly = true)
    public Page<ActividadResponseDTO> listar(Long proyectoId, Pageable pageable) {
        if (proyectoId != null) {
            return actividadRepository.findByProyectoId(proyectoId, pageable).map(this::toDTO);
        }
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
        Proyecto proyecto = proyectoRepository.findById(dto.idProyecto())
            .orElseThrow(() -> new ResourceNotFoundException("Proyecto", dto.idProyecto()));
        Hito hito = buscarHitoDelProyecto(dto.idHito(), dto.idProyecto());
        Actividad actividad = Actividad.builder()
            .nombre(dto.nombre())
            .descripcion(dto.descripcion())
            .fechaInicio(dto.fechaInicio())
            .fechaFin(dto.fechaFin())
            .estado(dto.estado() != null ? dto.estado() : EstadoActividad.PENDIENTE)
            .porcentajeAvance(dto.estado() == EstadoActividad.FINALIZADA ? 100 : 0)
            .proyecto(proyecto)
            .hito(hito)
            .build();

        if (dto.idResponsables() != null && !dto.idResponsables().isEmpty()) {
            actividad.setResponsables(new HashSet<>(usuarioRepository.findAllById(dto.idResponsables())));
        }

        Actividad guardada = actividadRepository.save(actividad);
        avanceProyectoService.recalcularProyecto(proyecto.getId());
        return toDTO(guardada);
    }

    @Transactional
    public ActividadResponseDTO actualizar(Long id, ActividadUpdateDTO dto) {
        Actividad a = buscarOFallar(id);
        Long proyectoAnteriorId = a.getProyecto().getId();
        Proyecto proyecto = proyectoRepository.findById(dto.idProyecto())
            .orElseThrow(() -> new ResourceNotFoundException("Proyecto", dto.idProyecto()));
        Hito hito = buscarHitoDelProyecto(dto.idHito(), dto.idProyecto());

        a.setNombre(dto.nombre());
        a.setDescripcion(dto.descripcion());
        a.setFechaInicio(dto.fechaInicio());
        a.setFechaFin(dto.fechaFin());
        if (dto.estado() != null) {
            a.setEstado(dto.estado());
            a.setPorcentajeAvance(dto.estado() == EstadoActividad.FINALIZADA ? 100 : 0);
        }
        a.setProyecto(proyecto);
        a.setHito(hito);

        if (dto.idResponsables() != null) {
            a.setResponsables(new HashSet<>(usuarioRepository.findAllById(dto.idResponsables())));
        }

        Actividad guardada = actividadRepository.save(a);
        avanceProyectoService.recalcularProyecto(proyectoAnteriorId);
        if (!proyectoAnteriorId.equals(proyecto.getId())) avanceProyectoService.recalcularProyecto(proyecto.getId());
        return toDTO(guardada);
    }

    @Transactional
    public ActividadResponseDTO cambiarEstado(Long id, EstadoActividad nuevoEstado) {
        Actividad a = buscarOFallar(id);
        a.setEstado(nuevoEstado);
        a.setPorcentajeAvance(nuevoEstado == EstadoActividad.FINALIZADA ? 100 : 0);
        Actividad guardada = actividadRepository.save(a);
        avanceProyectoService.recalcularProyecto(a.getProyecto().getId());
        return toDTO(guardada);
    }

    @Transactional
    public void eliminar(Long id) {
        Actividad a = buscarOFallar(id);
        Long proyectoId = a.getProyecto().getId();
        actividadRepository.delete(a);
        actividadRepository.flush();
        avanceProyectoService.recalcularProyecto(proyectoId);
    }

    @Transactional
    public ActividadResponseDTO actualizarAvance(Long id, Integer porcentajeAvance) {
        Actividad a = buscarOFallar(id);
        int avance = porcentajeAvance == null ? 0 : Math.max(0, Math.min(100, porcentajeAvance));
        a.setPorcentajeAvance(avance);
        a.setEstado(avance >= 100 ? EstadoActividad.FINALIZADA : avance > 0 ? EstadoActividad.EN_CURSO : EstadoActividad.PENDIENTE);
        Actividad guardada = actividadRepository.save(a);
        avanceProyectoService.recalcularProyecto(a.getProyecto().getId());
        return toDTO(guardada);
    }

    private Hito buscarHitoDelProyecto(Long idHito, Long idProyecto) {
        return hitoRepository.findByIdAndProyectoId(idHito, idProyecto)
            .orElseThrow(() -> new ResourceNotFoundException("Hito", idHito));
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
            a.getHito() != null ? a.getHito().getId() : null,
            a.getHito() != null ? a.getHito().getNombre() : null,
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
            s.getEstado(),
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
